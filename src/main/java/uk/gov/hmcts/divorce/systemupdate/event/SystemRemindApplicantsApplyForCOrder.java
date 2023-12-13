package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SystemRemindApplicantsApplyForCOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER = "system-remind-applicants-conditional-order";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Autowired
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER)
            .forStates(AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted)
            .name("Remind Applicants Apply for CO")
            .description("Remind Joint Applicants they can apply for a Conditional Order")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        Long id = details.getId();
        State state = details.getState();

        log.info("Calling to remind applicant's they can apply for a conditional order for case {} in state {}", id, state);

        if (AwaitingConditionalOrder.equals(state) || ConditionalOrderDrafted.equals(state)) {
            log.info("Awaiting conditional order notification firing for case {} in state {}", id, state);
            notificationDispatcher.send(awaitingConditionalOrderReminderNotification, data, id);
        } else {
            log.info("Conditional order pending reminder notification firing for case {} in state {}", id, state);
            notificationDispatcher.send(conditionalOrderPendingReminderNotification, data, id);
        }

        log.info(
            "20Week holding period +14days elapsed for Case({}) - reminding Joint Applicants they can apply for a Conditional Order",
            id
        );

        data.getApplication().setApplicantsRemindedCanApplyForConditionalOrder(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
