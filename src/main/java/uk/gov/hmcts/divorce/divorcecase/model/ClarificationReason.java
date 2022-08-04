package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ClarificationReason implements HasLabel {

    @JsonProperty("jurisdictionDetails")
    JURISDICTION_DETAILS("Jurisdiction details"),

    @JsonProperty("marriageCertTranslation")
    MARRIAGE_CERTIFICATE_TRANSLATION("Translation of marriage certificate"),

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage certificate"),

    @JsonProperty("previousProceedingDetails")
    PREVIOUS_PROCEEDINGS_DETAILS("Previous proceedings details"),

    @JsonProperty("other")
    OTHER("Enter free Text / Make free text order");

    private final String label;
}
