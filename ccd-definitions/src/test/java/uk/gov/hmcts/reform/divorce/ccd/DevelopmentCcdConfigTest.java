package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.WebhookConvention;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.types.Webhook.AboutToStart;

public class DevelopmentCcdConfigTest {

    private final DevelopmentCcdConfig developmentCcdConfig = new DevelopmentCcdConfig();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void shouldApplyAllDefaultCcdBuilders() {

        final ArgumentCaptor<WebhookConvention> webhookConventionCaptor = ArgumentCaptor.forClass(WebhookConvention.class);
        final ConfigBuilder configBuilder = mock(ConfigBuilder.class);
        final CcdBuilderApplier ccdBuilderApplier = mock(CcdBuilderApplier.class);
        developmentCcdConfig.ccdBuilderApplier = ccdBuilderApplier;

        doNothing().when(configBuilder).setWebhookConvention(webhookConventionCaptor.capture());

        developmentCcdConfig.configure(configBuilder);

        final WebhookConvention webhookConvention = webhookConventionCaptor.getValue();
        assertThat(webhookConvention.buildUrl(AboutToStart, "eventId"), is("localhost:4013/eventId/AboutToStart"));

        verify(configBuilder).setEnvironment("development");
        verify(configBuilder).setWebhookConvention(any());
        verify(ccdBuilderApplier).applyWith(configBuilder);

        verifyNoMoreInteractions(configBuilder, ccdBuilderApplier);
    }
}