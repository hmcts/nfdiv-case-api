package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemRemindApplicantsApplyForCOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER = "system-remind-applicants-conditional-order";

    @Autowired
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Autowired
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

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

        if (AwaitingConditionalOrder.equals(details.getState()) || ConditionalOrderDrafted.equals(details.getState())) {
            notificationDispatcher.send(awaitingConditionalOrderReminderNotification, data, details.getId());
        } else {
            notificationDispatcher.send(conditionalOrderPendingReminderNotification, data, details.getId());
        }

        data.getApplication().setApplicantsRemindedCanApplyForConditionalOrder(YES);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
