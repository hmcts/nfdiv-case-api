package uk.gov.hmcts.divorce.common.model;

import java.util.List;

public interface CaseState {

    default List<String> validate(CaseData data) {
        return null;
    }
}
