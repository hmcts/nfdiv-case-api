package uk.gov.hmcts.divorce.common;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;

import java.util.List;

@Builder
@Getter
public class CaseInfo {

    private final CaseData caseData;
    private final State state;
    private final List<String> errors;
}
