package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

public final class DocManagementStoreWireMock {

    private static final WireMockServer DM_STORE_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private DocManagementStoreWireMock() {
    }

    public static void start() {
        if (!DM_STORE_SERVER.isRunning()) {
            DM_STORE_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (DM_STORE_SERVER.isRunning()) {
            DM_STORE_SERVER.stop();
            DM_STORE_SERVER.resetAll();
        }
    }

    public static void stubForDocumentManagement(final String documentUuid, final HttpStatus httpStatus) {
        DM_STORE_SERVER.stubFor(delete("/documents/" + documentUuid + "?permanent=true")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-id", new EqualToPattern("1"))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce-solicitor"))
            .willReturn(aResponse().withStatus(httpStatus.value())));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("document_management.url=" + "http://localhost:" + DM_STORE_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
