package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeeItem {
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private FeeValue value;

}
