package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ConditionalOrderCourt implements HasLabel {

    @JsonProperty("birmingham")
    BIRMIGHAM("birmingham", "Birmingham Civil and Family Justice Centre"),

    @JsonProperty("buryStEdmunds")
    BURY_ST_EDMUNDS("buryStEdmunds", "Bury St. Edmunds Regional Divorce Centre");

    private String courtId;
    private String label;
}
