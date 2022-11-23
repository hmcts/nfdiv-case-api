package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
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
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateNFDCase.SYSTEM_UPDATE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindApplicantsApplyForCOrderTask implements Runnable {

    public static final String NOTIFICATION_FLAG = "applicantsRemindedCanApplyForConditionalOrder";
    public static final String SUBMIT_EVENT_ERROR = "Submit event failed for Case Id: {}, State: {}, continuing to next case";
    public static final String DESERIALIZATION_ERROR = "Deserialization failed for Case Id: {}, continuing to next case";
    public static final String CCD_SEARCH_ERROR =
        "SystemRemindApplicantsApplyForCOrderTask scheduled task stopped after search error";
    public static final String CCD_CONFLICT_ERROR =
        "SystemRemindApplicantsApplyForCOrderTask scheduled task stopping due to conflict with another running task";
    private static final int MAX_RETRIES = 5;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Autowired
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${submit_co.reminder_offset_days}")
    private int submitCOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("Remind Joint Applicants that they can apply for a Conditional Order");
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(
                        boolQuery()
                            .should(matchQuery(STATE, AwaitingConditionalOrder))
                            .should(matchQuery(STATE, ConditionalOrderPending))
                            .should(matchQuery(STATE, ConditionalOrderDrafted))
                            .minimumShouldMatch(1)
                    )
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now().minusDays(submitCOrderReminderOffsetDays)))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization,
                AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted)
                    .forEach(caseDetails -> remindJointApplicants(caseDetails, user, serviceAuthorization));

            log.info("SystemRemindApplicantsApplyForCOrderTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (final CcdConflictException e) {
            log.info(CCD_CONFLICT_ERROR);
        }
    }

    private void remindJointApplicants(CaseDetails caseDetails, User user, String serviceAuth) {

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        String state = caseDetails.getState();

        try {
            if (AwaitingConditionalOrder.name().equals(state) || ConditionalOrderDrafted.name().equals(state)) {
                notificationDispatcher.send(awaitingConditionalOrderReminderNotification, caseData, caseDetails.getId());
            } else {
                notificationDispatcher.send(conditionalOrderPendingReminderNotification, caseData, caseDetails.getId());
            }
        } catch (NotificationException | HttpServerErrorException exception) {
            log.error("Notification for SystemRemindApplicantsApplyForCOrderTask has failed with exception {} for case id {}",
                exception.getMessage(), caseDetails.getId());

            if (caseData.getConditionalOrder().getCronRetriesRemindApplicantApplyCo() == null) {
                caseData.getConditionalOrder().setCronRetriesRemindApplicantApplyCo(0);
            }

            if (caseData.getConditionalOrder().getCronRetriesRemindApplicantApplyCo() < MAX_RETRIES) {
                caseData.getConditionalOrder().setCronRetriesRemindApplicantApplyCo(
                    caseData.getConditionalOrder().getCronRetriesRemindApplicantApplyCo() + 1);

                caseDetails.setData(objectMapper.convertValue(caseData, new TypeReference<>() {}));
                ccdUpdateService.submitEvent(caseDetails, SYSTEM_UPDATE_CASE, user, serviceAuth);
            }

            // return as we don't want to continue and call the 'remind applicant' event.
            return;
        }

        // If notifications are successful, continue to call the system remind applications CO event.
        try {
            log.info(
                "20Week holding period +14days elapsed for Case({}) - reminding Joint Applicants they can apply for a Conditional Order",
                caseDetails.getId()
            );
            caseDetails.setData(objectMapper.convertValue(caseData, new TypeReference<>() {}));
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error(SUBMIT_EVENT_ERROR, caseDetails.getId(), caseDetails.getState());
        } catch (final IllegalArgumentException e) {
            log.error(DESERIALIZATION_ERROR, caseDetails.getId());
        }
    }
}
