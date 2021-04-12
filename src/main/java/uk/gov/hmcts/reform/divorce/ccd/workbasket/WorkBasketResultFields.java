package uk.gov.hmcts.reform.divorce.ccd.workbasket;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class WorkBasketResultFields implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .workBasketResultFields()
            .caseReferenceField();
    }
}
