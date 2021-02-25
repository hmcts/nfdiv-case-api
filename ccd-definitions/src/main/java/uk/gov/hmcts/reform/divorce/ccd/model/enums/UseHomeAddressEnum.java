package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum UseHomeAddressEnum implements HasLabel {

    @JsonProperty("Yes")
    YES("Yes - use home address"),

    @JsonProperty("No")
    NO("No - use another address"),

    @JsonProperty("Solicitor")
    SOLICITOR("Solicitor - use solicitors address");

    private final String label;
}
