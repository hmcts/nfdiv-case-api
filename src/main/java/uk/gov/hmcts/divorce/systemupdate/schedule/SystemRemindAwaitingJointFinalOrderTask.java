package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindAwaitingJointFinalOrder.SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindAwaitingJointFinalOrderTask implements Runnable {

    public static final String NOTIFICATION_SENT_FLAG = String.format(DATA, "applicantsRemindedAwaitingJointFinalOrder");
    public static final String DATE_FINAL_ORDER_SUBMITTED = String.format(DATA, "dateFinalOrderSubmitted");

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${final_order.awaiting_joint_final_order_reminder_offset_days}")
    private int awaitingJointFinalOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("SystemRemindAwaitingJointFinalOrderTask started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingJointFinalOrder))
                    .must(existsQuery(DATE_FINAL_ORDER_SUBMITTED))
                    .filter(rangeQuery(DATE_FINAL_ORDER_SUBMITTED)
                        .lte(LocalDate.now().minusDays(awaitingJointFinalOrderReminderOffsetDays)))
                    .mustNot(matchQuery(NOTIFICATION_SENT_FLAG, YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingJointFinalOrder)
                .forEach(caseDetails -> triggerJointFinalOrderReminder(user, serviceAuth, caseDetails.getId()));

            log.info("SystemRemindAwaitingJointFinalOrderTask complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRemindAwaitingJointFinalOrderTask stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.error("SystemRemindAwaitingJointFinalOrderTask stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerJointFinalOrderReminder(User user, String serviceAuth, Long caseId) {
        try {
            log.info("Submitting Remind Awaiting Joint Final Order Event for Case {}", caseId);
            ccdUpdateService.submitEvent(caseId, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event Remind Awaiting Joint Final Order failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
