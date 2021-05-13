package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;

public final class FeesUtil {

    public static final WireMockServer FEES_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private FeesUtil() {
    }

    public static void stubForFeesLookup(final ObjectMapper objectMapper) throws JsonProcessingException {
        FEES_SERVER.stubFor(get("/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce"
            ).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getFeeResponse())))
        );
    }

    public static void stubForFeesNotFound() {
        FEES_SERVER.stubFor(get(urlEqualTo(
            "/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce"
            )
            ).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withStatusMessage("Fee event not found")
            )
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "fee.api.baseUrl=" + "http://localhost:" + FEES_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
