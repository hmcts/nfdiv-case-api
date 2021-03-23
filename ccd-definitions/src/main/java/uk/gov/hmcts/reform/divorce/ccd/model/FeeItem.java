package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class FeeItem {
    @JsonProperty("id")
    @CCD(
        label = "Fee Item Id"
    )
    private String id;

    @JsonProperty("value")
    @CCD(
        label = "Fee value"
    )
    private FeeValue value;

}
