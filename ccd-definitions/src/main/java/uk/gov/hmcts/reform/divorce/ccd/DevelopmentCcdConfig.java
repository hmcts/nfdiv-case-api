package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.CCDConfig;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Webhook;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class DevelopmentCcdConfig implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> builder) {

        builder.caseType("NO_FAULT_DIVORCE");
        builder.setEnvironment("development");
        builder.setWebhookConvention(this::webhookConvention);

        new BaseCcdConfig().buildWith(builder);
    }

    private String webhookConvention(final Webhook webhook, final String eventId) {
        return "localhost:4013/" + eventId + "/" + webhook;
    }
}
