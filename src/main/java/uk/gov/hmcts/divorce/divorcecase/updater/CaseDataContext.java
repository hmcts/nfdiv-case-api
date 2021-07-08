package uk.gov.hmcts.divorce.divorcecase.updater;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class CaseDataContext {

    private CaseData caseData;
    private Long caseId;
    private String userAuthToken;
    private LocalDate createdDate;

    public CaseData copyOfCaseData() {
        return caseData.toBuilder().build();
    }

    public CaseDataContext handlerContextWith(final CaseData caseData) {
        setCaseData(caseData);
        return this;
    }
}
