package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.birmingham;
import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.buryStEdmunds;

@Getter
@AllArgsConstructor
public enum ConditionalOrderCourt implements HasLabel {

    @JsonProperty("birmingham")
    BIRMINGHAM("birmingham", birmingham),

    @JsonProperty("buryStEdmunds")
    BURY_ST_EDMUNDS("buryStEdmunds", buryStEdmunds);

    private String courtId;
    private String label;
}
