package uk.gov.hmcts.divorce.common.model;

import java.util.List;

public interface CaseState {

    List<String> validate(CaseData data);
}
