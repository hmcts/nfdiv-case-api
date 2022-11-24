package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@NoArgsConstructor
@Getter
@Setter
public class SentNotifications {

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant1;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant1Offline;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant2;

    private YesOrNo awaitingConditionalOrderReminderNotificationSendToApplicant2Offline;

    @JsonIgnore
    public boolean hasAwaitingConditionalOrderReminderNotificationSendToApplicant1() {
        return YES.equals(getAwaitingConditionalOrderReminderNotificationSendToApplicant1());
    }

    @JsonIgnore
    public boolean hasAwaitingConditionalOrderReminderNotificationSendToApplicant2() {
        return YES.equals(getAwaitingConditionalOrderReminderNotificationSendToApplicant2());
    }

    @JsonIgnore
    public boolean hasAwaitingConditionalOrderReminderNotificationSendToApplicant1Offline() {
        return YES.equals(getAwaitingConditionalOrderReminderNotificationSendToApplicant1Offline());
    }

    @JsonIgnore
    public boolean hasAwaitingConditionalOrderReminderNotificationSendToApplicant2Offline() {
        return YES.equals(getAwaitingConditionalOrderReminderNotificationSendToApplicant2Offline());
    }
}
