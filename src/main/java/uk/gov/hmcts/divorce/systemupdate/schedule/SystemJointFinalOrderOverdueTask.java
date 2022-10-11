package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindJointFinalOrderOverdue.SYSTEM_JOINT_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemJointFinalOrderOverdueTask implements Runnable {

    public static final String NOTIFICATION_SENT_FLAG = String.format(DATA, "applicantsRemindedJointFinalOrderOverdue");
    private static final String DATE_FINAL_ORDER_SUBMITTED = String.format(DATA, "dateFinalOrderSubmitted");

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${final_order.joint_final_order_overdue_offset_days}")
    private int jointFinalOrderOverdueOffsetDays;

    @Override
    public void run() {
        log.info("Joint Final Order overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingJointFinalOrder))
                    .must(existsQuery(DATE_FINAL_ORDER_SUBMITTED))
                    .filter(rangeQuery(DATE_FINAL_ORDER_SUBMITTED)
                        .lte(LocalDate.now().minusDays(jointFinalOrderOverdueOffsetDays)))
                    .mustNot(matchQuery(NOTIFICATION_SENT_FLAG, YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingJointFinalOrder)
                .forEach(caseDetails -> triggerJointFinalOrderReminder(user, serviceAuth, caseDetails));

            log.info("Joint Final Order overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Joint Final Order overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Joint Final Order overdue schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerJointFinalOrderReminder(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Joint Final Order Overdue Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_JOINT_FINAL_ORDER_OVERDUE, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Joint Final Order Overdue Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
