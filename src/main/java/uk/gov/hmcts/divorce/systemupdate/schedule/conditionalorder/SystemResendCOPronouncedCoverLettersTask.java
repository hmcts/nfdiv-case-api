package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

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
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyFinalOrderOverdue.SYSTEM_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any cases which are in 'ConditionalOrderPronounced' state and where the applicants are offline and have contact details private
 * 'Conditional order pronounced cover letters' should be regenerated and send to respective applicant.
 */
public class SystemResendCOPronouncedCoverLettersTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String NOTIFICATION_FLAG = "coPronouncedCoverLetterResent";

    @Override
    public void run() {
        log.info("Final Order overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, ConditionalOrderPronounced))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            final List<CaseDetails> casesToBeUpdated =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, ConditionalOrderPronounced);

            for (final CaseDetails caseDetails : casesToBeUpdated) {
                triggerResendCoPronouncedCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("Final Order overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Final Order overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Final Order overdue schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerResendCoPronouncedCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {

            Map<String, Object> caseDataMap = caseDetails.getData();
            log.info("Submitting Final Order Overdue Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_FINAL_ORDER_OVERDUE, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
