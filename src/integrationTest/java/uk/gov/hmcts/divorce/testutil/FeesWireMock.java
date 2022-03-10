package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public final class FeesWireMock {

    private static final WireMockServer FEES_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private FeesWireMock() {
    }

    public static void start() {
        if (!FEES_SERVER.isRunning()) {
            FEES_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (FEES_SERVER.isRunning()) {
            FEES_SERVER.stop();
            FEES_SERVER.resetAll();
        }
    }

    public static void stubForFeesLookup(final String feeResponse) {
        FEES_SERVER.stubFor(get("/fees-register/fees/lookup"
            + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce&keyword=DivorceCivPart")
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(feeResponse)));
    }

    public static void stubForFeesLookup(final String feeResponse, final String event, final String service, final String keyword) {
        String url = String.format("/fees-register/fees/lookup"
            + "?channel=default&event=%s&jurisdiction1=family&jurisdiction2=family+court&service=%s&keyword=%s",
            event, service, keyword);

        FEES_SERVER.stubFor(get(url)
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(feeResponse)));
    }

    public static void stubForFeesNotFound() {
        FEES_SERVER.stubFor(get(urlEqualTo(
            "/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce&keyword=DivorceCivPart"))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withStatusMessage("Fee event not found")));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("fee.api.baseUrl=" + "http://localhost:" + FEES_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
