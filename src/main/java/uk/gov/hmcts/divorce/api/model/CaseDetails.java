package uk.gov.hmcts.divorce.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.ccd.model.CaseData;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CaseDetails {

    @JsonProperty("case_data")
    private CaseData caseData;

    @JsonProperty("id")
    private String caseId;

    private String state;
}
