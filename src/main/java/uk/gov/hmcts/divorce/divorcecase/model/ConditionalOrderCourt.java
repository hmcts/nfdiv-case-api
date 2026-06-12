package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.birmingham;
import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.birminghamCourtId;
import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.buryStEdmunds;
import static uk.gov.hmcts.divorce.divorcecase.constant.ConditionalOrderCourtConstants.buryStEdmundsCourtId;

@Getter
@AllArgsConstructor
public enum ConditionalOrderCourt implements HasLabel {

    @JsonProperty(birminghamCourtId)
    BIRMINGHAM(birminghamCourtId, birmingham),

    @JsonProperty(buryStEdmundsCourtId)
    BURY_ST_EDMUNDS(buryStEdmundsCourtId, buryStEdmunds);

    private String courtId;
    private String label;
}
