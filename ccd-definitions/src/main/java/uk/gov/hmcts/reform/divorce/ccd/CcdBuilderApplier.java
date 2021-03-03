package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class CcdBuilderApplier {

    private final CcdBuilderFactory ccdBuilderFactor;

    public CcdBuilderApplier(final CcdBuilderFactory ccdBuilderFactory) {
        this.ccdBuilderFactor = ccdBuilderFactory;
    }

    public void applyWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        ccdBuilderFactor.getCcdBuilders().forEach(ccdBuilder -> ccdBuilder.buildWith(configBuilder));
    }
}
