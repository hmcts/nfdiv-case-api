package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public interface CcdBuilder {

    void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder);
}
