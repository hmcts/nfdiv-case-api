package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum FinancialOrderFor implements HasLabel {

    @JsonProperty("children")
    CHILDREN("Children"),

    @JsonProperty("petitioner")
    PETITIONER("Petitioner");

    private final String label;
}
