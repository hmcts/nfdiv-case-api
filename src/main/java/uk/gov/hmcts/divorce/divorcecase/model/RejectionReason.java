package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RejectionReason implements HasLabel {

    @JsonProperty("noJurisdiction")
    NO_JURISDICTION("Court does not have jurisdiction"),

    @JsonProperty("noCriteria")
    NO_CRITERIA("Applicant does not fit criteria for divorce"),

    @JsonProperty("insufficentDetails")
    INSUFFICIENT_DETAILS("Insufficent details in application"),

    @JsonProperty("other")
    OTHER("Provide details / Make free text order");

    private final String label;
}
