package uk.gov.hmcts.divorce.divorcecase.validation;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

@Getter
public class SolicitorPbaValidation {
    private final DynamicList pbaNumbersList;
    private final AboutToStartOrSubmitResponse<CaseData, State> errorResponse;

    private SolicitorPbaValidation(DynamicList pbaNumbersList, AboutToStartOrSubmitResponse<CaseData, State> errorResponse) {
        this.pbaNumbersList = pbaNumbersList;
        this.errorResponse = errorResponse;
    }

    public static SolicitorPbaValidation success(DynamicList pbaList) {
        return new SolicitorPbaValidation(pbaList, null);
    }

    public static SolicitorPbaValidation error(CaseData caseData, String errorMessage) {
        return new SolicitorPbaValidation(null,
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(errorMessage))
                .build());
    }

    public boolean isEmpty() {
        return pbaNumbersList == null;
    }
}
