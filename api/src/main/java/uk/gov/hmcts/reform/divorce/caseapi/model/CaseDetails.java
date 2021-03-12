package uk.gov.hmcts.reform.divorce.caseapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class CaseDetails {

    @JsonProperty("case_data")
    private CaseData caseData;

    @JsonProperty("id")
    private String caseId;

    private String state;
}
