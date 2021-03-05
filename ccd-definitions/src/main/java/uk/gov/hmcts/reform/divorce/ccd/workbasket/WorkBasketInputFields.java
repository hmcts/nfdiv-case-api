package uk.gov.hmcts.reform.divorce.ccd.workbasket;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfig;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class WorkBasketInputFields implements CcdConfig {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .workBasketInputFields()
            .caseReferenceField();
    }
}
