package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class CcdConfigApplier {

    private final CcdConfigFactory ccdBuilderFactor;

    public CcdConfigApplier(final CcdConfigFactory ccdConfigFactory) {
        this.ccdBuilderFactor = ccdConfigFactory;
    }

    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        ccdBuilderFactor.getCcdConfig().forEach(ccdConfig -> ccdConfig.applyTo(configBuilder));
    }
}
