package uk.gov.hmcts.reform.divorce.caseapi.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class CaseDataContext {

    private CaseData caseData;
    private String s2sAuthToken;
    private String caseId;
    private String userAuthToken;

    public CaseData copyOfCaseData() {
        return caseData.toBuilder().build();
    }

    public CaseDataContext handlerContextWith(final CaseData caseData) {
        setCaseData(caseData);
        return this;
    }
}
