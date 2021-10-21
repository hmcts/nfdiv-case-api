package uk.gov.hmcts.divorce.common.ccd;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public interface CcdPageConfiguration {
    String NEVER_SHOW = "divorceOrDissolution=\"NEVER_SHOW\"";

    void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder);
}
