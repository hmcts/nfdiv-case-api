package uk.gov.hmcts.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public interface CcdConfiguration {

    void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder);
}
