package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindRespondentSolicitor.SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_REPRESENTED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.COURT_SERVICE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SERVICE_METHOD;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindRespondentSolicitorToRespondTask implements Runnable {

    private static final String NOTIFICATION_SENT_FLAG = "respondentSolicitorReminderSent";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${respondent_solicitor.response_offset_days}")
    private int responseReminderOffsetDays;

    @Autowired
    private Clock clock;

    @Override
    public void run() {
        log.info("Remind respondent solicitor to respond task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, AwaitingAos))
                .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
                .must(matchQuery(String.format(DATA, APPLICANT2_REPRESENTED), YesOrNo.YES))
                .must(matchQuery(String.format(DATA, SERVICE_METHOD), COURT_SERVICE))
                .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now(clock).minusDays(responseReminderOffsetDays)))
                .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

            List<CaseDetails> result = ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingAos);

            log.info("Number of cases found for reminder : {}", CollectionUtils.isEmpty(result) ? 0 : result.size());

            emptyIfNull(result)
                .stream()
                .filter(caseDetails -> caseDetails.getData().get("applicant2SolicitorOrganisationPolicy") != null)
                .forEach(caseDetails -> sendReminderToRespondentSolicitor(caseDetails, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("SystemRemindRespondentSolicitorToRespondTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRemindRespondentSolicitorToRespondTask schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void sendReminderToRespondentSolicitor(CaseDetails caseDetails, User user, String serviceAuthorization) {
        try {
            log.info("Submitting system-remind-respondent-solicitor-to-respond event...");
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, serviceAuthorization);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
