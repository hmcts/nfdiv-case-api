package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.WebhookConvention;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.mock.SearchBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.mock.TabBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.mock.WorkBasketBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.api.Webhook.AboutToStart;

@ExtendWith(MockitoExtension.class)
class DevelopmentCcdConfigTest {

    private final DevelopmentCcdConfig developmentCcdConfig = new DevelopmentCcdConfig();

    @Mock(lenient = true)
    private ConfigBuilder<CaseData, State, UserRole> configBuilder;

    @BeforeEach
    void setupConfigBuilderMocks() {
        new EventBuildingMockUtil().mockEventBuildingWith(configBuilder);
        new SearchBuildingMockUtil().mockSearchBuildingWith(configBuilder);
        new TabBuildingMockUtil().mockTabBuildingWith(configBuilder);
        new WorkBasketBuildingMockUtil().mockWorkBasketBuildingWith(configBuilder);
    }

    @Test
    void shouldApplyAllCcdBuilders() {

        final ArgumentCaptor<WebhookConvention> webhookConventionCaptor = ArgumentCaptor.forClass(WebhookConvention.class);

        doNothing().when(configBuilder).setWebhookConvention(webhookConventionCaptor.capture());

        developmentCcdConfig.configure(configBuilder);

        final WebhookConvention webhookConvention = webhookConventionCaptor.getValue();
        assertThat(webhookConvention.buildUrl(AboutToStart, "eventId"), is("localhost:4013/eventId/AboutToStart"));

        verify(configBuilder).setEnvironment("development");
        verify(configBuilder).setWebhookConvention(any());
    }
}
