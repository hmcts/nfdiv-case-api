package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant2.SYSTEM_REMIND_APPLICANT2;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ACCESS_CODE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemRemindApplicant2Task implements Runnable {

    private static final int FOUR_DAYS = 4;
    private static final String NOTIFICATION_FLAG = "applicant2ReminderSent";

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final ObjectMapper objectMapper;

    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("Remind applicant 2 scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingApplicant2Response))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now().plusDays(FOUR_DAYS)))
                    .must(existsQuery(ACCESS_CODE))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            final List<CaseDetails> casesInAwaitingApplicant2Response =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingApplicant2Response);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant2Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate reminderDate = caseData.getDueDate().minusDays(FOUR_DAYS);
                    log.info("Due Date for case id: {} is {}", caseDetails.getId(), caseData.getDueDate());

                    if (!reminderDate.isAfter(LocalDate.now()) && caseData.getCaseInvite().accessCode() != null
                        && !caseData.getApplication().hasApplicant2ReminderBeenSent()
                    ) {
                        notifyApplicant2(caseDetails.getId(), reminderDate, user, serviceAuthorization);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Remind applicant 2 scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Remind applicant 2 schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 2 scheduled task stopping"
                + " due to conflict with another running Remind applicant 2 task"
            );
        }
    }

    private void notifyApplicant2(Long caseId, LocalDate reminderDate, User user, String serviceAuth) {
        log.info("Reminder date {} for Case id {} is on/before current date - sending reminder to Applicant 2",
            reminderDate,
            caseId
        );

        ccdUpdateService.submitEvent(caseId, SYSTEM_REMIND_APPLICANT2, user, serviceAuth);
    }
}
