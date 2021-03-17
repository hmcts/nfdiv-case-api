package uk.gov.hmcts.reform.divorce.ccd;

import lombok.SneakyThrows;
import org.reflections.Reflections;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class DevelopmentCcdConfig implements CCDConfig<CaseData, State, UserRole> {

    @SneakyThrows
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.setEnvironment("development");
        configBuilder.setWebhookConvention(this::webhookConvention);

        Reflections reflections = new Reflections("uk.gov.hmcts.reform.divorce.ccd");

        for (Class<? extends CcdConfiguration> configClass : reflections.getSubTypesOf(CcdConfiguration.class)) {
            CcdConfiguration inst = configClass.getDeclaredConstructor().newInstance();
            inst.applyTo(configBuilder);
        }
    }

    private String webhookConvention(final Webhook webhook, final String eventId) {
        String caseApiUrl = System.getenv("CASE_API_URL");
        return (caseApiUrl == null ? "http://nfdiv-case-api:4013" : caseApiUrl) + "/" + eventId + "/" + webhook;
    }
}
