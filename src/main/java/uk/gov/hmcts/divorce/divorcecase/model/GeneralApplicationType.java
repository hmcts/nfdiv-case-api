package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralApplicationType implements HasLabel {

    @JsonProperty("dispensedWithService")
    DISPENSED_WITH_SERVICE("Dispensed with service"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service"),

    @JsonProperty("issueDivorceWithoutMarriageCertificate")
    ISSUE_DIVORCE_WITHOUT_CERT("Issue divorce without marriage certificate"),

    @JsonProperty("expedite")
    EXPEDITE("Expedite"),

    @JsonProperty("otherAlternativeServiceMethod")
    OTHER_ALTERNATIVE_SERVICE_METHODS("Other alternative service methods"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
