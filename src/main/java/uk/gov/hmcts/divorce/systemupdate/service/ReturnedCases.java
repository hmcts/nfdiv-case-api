package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnedCases {

    @JsonProperty(value = "cases")
    private final List<ReturnedCaseDetails> cases;

    @JsonProperty(value = "total")
    private final int total;
}
