package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

@NoArgsConstructor
@Getter
@Setter
public class SentNotifications {

    private YesOrNo sentAwaitingConditionalOrderReminderNotificationSendToApplicant1;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant1Offline;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant2;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant2Offline;
}
