package uk.gov.hmcts.divorce.model;

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
// TODO remove in favour of CCD generator lib
public class CaseDetails {

    @JsonProperty("case_data")
    private CaseData caseData;

    @JsonProperty("id")
    private Long caseId;

    private String state;
}
