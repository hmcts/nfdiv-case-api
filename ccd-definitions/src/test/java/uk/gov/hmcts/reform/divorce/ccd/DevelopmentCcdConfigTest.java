package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.WebhookConvention;

import static org.mockito.Mockito.mock;

public class DevelopmentCcdConfigTest {

    private final DevelopmentCcdConfig developmentCcdConfig = new DevelopmentCcdConfig();

    //TODO: Fix the testing here
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void shouldApplyAllCcdBuilders() {

        final ArgumentCaptor<WebhookConvention> webhookConventionCaptor = ArgumentCaptor.forClass(WebhookConvention.class);
        final ConfigBuilder configBuilder = mock(ConfigBuilder.class);

//        doNothing().when(configBuilder).setWebhookConvention(webhookConventionCaptor.capture());

//        developmentCcdConfig.configure(configBuilder);
//
//        final WebhookConvention webhookConvention = webhookConventionCaptor.getValue();
//        assertThat(webhookConvention.buildUrl(AboutToStart, "eventId"), is("localhost:4013/eventId/AboutToStart"));
//
//        verify(configBuilder).setEnvironment("development");
//        verify(configBuilder).setWebhookConvention(any());
    }
}