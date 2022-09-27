package uk.gov.hmcts.divorce.divorcecase.model.notificationTrackers;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class NotificationSentTracker {

    @JsonUnwrapped(prefix = "awaitingConditionalOrderNotification")
    @Builder.Default
    private AwaitingConditionalOrderNotificationSentTracker awaitingConditionalOrderNotificationSentTracker
        = new AwaitingConditionalOrderNotificationSentTracker();
}
