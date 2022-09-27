package uk.gov.hmcts.divorce.divorcecase.model.notificationTrackers;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AwaitingConditionalOrderNotificationSentTracker {

    @Builder.Default
    public boolean alreadySentToApplicant1;

    @Builder.Default
    public boolean alreadySentToApplicant1Solicitor;

    @Builder.Default
    public boolean alreadySentToApplicant1Offline;

    @Builder.Default
    public boolean alreadySentToApplicant2;

    @Builder.Default
    public boolean alreadySentToApplicant2Solicitor;

    @Builder.Default
    public boolean alreadySentToApplicant2Offline;
}
