package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.CCDConfig;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Webhook;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class DevelopmentCcdConfig implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.setEnvironment("development");
        configBuilder.setWebhookConvention(this::webhookConvention);

        new CcdBuilderApplier(new DefaultCcdBuilderFactory()).applyWith(configBuilder);
    }

    private String webhookConvention(final Webhook webhook, final String eventId) {
        return "localhost:4013/" + eventId + "/" + webhook;
    }
}
