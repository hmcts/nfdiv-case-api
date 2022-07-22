package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantsSwitchToSoleCoOverdue.SYSTEM_CONDITIONAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any joint cases which are in 'ConditionalOrderPending (Awaiting Joint Conditional Order)' state and where the current date is
 * 14 days after the first in time applicant submitted their conditional order but the second in time has not submitted their conditional order,
 * then notify the first in time applicant that they can switch to sole.
 */
public class SystemNotifyApplicantsApplySwitchToSoleCoOverdueTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String APPLICANT1_CONDITIONAL_ORDER_SUBMITTED_DATE = "coApplicant1SubmittedDate";
    private static final String APPLICANT1_IS_REPRESENTED = "applicant1SolicitorRepresented";

    private static final String APPLICANT2_CONDITIONAL_ORDER_SUBMITTED_DATE = "coApplicant2SubmittedDate";
    private static final String APPLICANT2_IS_REPRESENTED = "applicant1SolicitorRepresented";

    @Override
    public void run() {
        log.info("Applicants can apply for switch to sole (conditional order overdue) scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, ConditionalOrderPending))
                    .must(matchQuery(String.format(DATA, APPLICATION_TYPE), JOINT_APPLICATION));

            BoolQueryBuilder applicant1Query =
                boolQuery()
                    .filter(rangeQuery(APPLICANT1_CONDITIONAL_ORDER_SUBMITTED_DATE).lte(LocalDate.now().minusDays(14)))
                    .filter(matchQuery(String.format(DATA, APPLICANT1_IS_REPRESENTED), YesOrNo.YES));

            BoolQueryBuilder applicant2Query =
                boolQuery()
                    .filter(rangeQuery(APPLICANT2_CONDITIONAL_ORDER_SUBMITTED_DATE).lte(LocalDate.now().minusDays(14)))
                    .filter(matchQuery(String.format(DATA, APPLICANT2_IS_REPRESENTED), YesOrNo.YES));

            query.should(applicant1Query).should(applicant2Query);

            List<CaseDetails> jointCasesInConditionalOrderPendingEligibleForSwitchToSole =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, ConditionalOrderPending);

            for (final CaseDetails caseDetails : jointCasesInConditionalOrderPendingEligibleForSwitchToSole) {
                triggerSwitchToSoleCoOverdueNotificationForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("Applicants can apply for switch to sole (conditional order overdue) scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Applicants can apply for switch to sole (conditional order overdue) scheduled task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Applicants can apply for switch to sole (conditional order overdue) scheduled task started stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerSwitchToSoleCoOverdueNotificationForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Final Order Overdue Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_CONDITIONAL_ORDER_OVERDUE, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
