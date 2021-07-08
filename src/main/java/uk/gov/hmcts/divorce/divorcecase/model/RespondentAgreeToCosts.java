package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RespondentAgreeToCosts implements HasLabel {

    @JsonProperty("Yes")
    YES("The respondent agrees to pay all the costs"),

    @JsonProperty("No")
    NO("The respondent does not agrees to pay any of the costs and gives their reasons"),

    @JsonProperty("DifferentAmount")
    DIFFERENT_AMOUNT("The respondent agrees to pay some of the costs and gives their reasons");

    private final String label;
}
