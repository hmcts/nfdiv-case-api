package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JudicialSeparationReissueOption implements HasLabel {
    @JsonProperty("offlineAos")
    OFFLINE_AOS("Offline AoS"),

    @JsonProperty("reissueCase")
    REISSUE_CASE("Reissue Case");

    private final String label;
}
