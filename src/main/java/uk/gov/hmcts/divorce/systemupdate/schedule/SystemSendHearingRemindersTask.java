package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingOutcome;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendHearingReminder.SYSTEM_SEND_HEARING_REMINDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSendHearingRemindersTask implements Runnable {

    public static final int EARLIEST_REMINDER_DAYS_BEFORE_HEARING = 14;
    public static final int LATEST_REMINDER_DAYS_BEFORE_HEARING = 7;
    public static final String DATE_OF_HEARING_CASE_FIELD = "dateOfHearing";
    public static final String HEARING_REMINDER_NOTIFICATION_FLAG = "hasHearingReminderBeenSent";

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("SystemSendHearingRemindersTask scheduled task started");

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, PendingHearingOutcome))
                    .filter(rangeQuery(DATE_OF_HEARING_CASE_FIELD)
                        .gte(LocalDate.now().plusDays(LATEST_REMINDER_DAYS_BEFORE_HEARING))
                        .lte(LocalDate.now().plusDays(EARLIEST_REMINDER_DAYS_BEFORE_HEARING)))
                    .mustNot(matchQuery(String.format(DATA, HEARING_REMINDER_NOTIFICATION_FLAG), YesOrNo.YES));

            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            ccdSearchService
                .searchForAllCasesWithQuery(query, user, serviceAuth, PendingHearingOutcome)
                .forEach(caseDetails -> submitEvent(caseDetails.getId(), user, serviceAuth));

            log.info("SystemSendHearingRemindersTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemSendHearingRemindersTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemSendHearingRemindersTask schedule task stopping due to conflict with another running task"
            );
        }
    }

    private void submitEvent(Long caseId, User user, String serviceAuth) {
        try {
            ccdUpdateService.submitEvent(caseId, SYSTEM_SEND_HEARING_REMINDER, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
