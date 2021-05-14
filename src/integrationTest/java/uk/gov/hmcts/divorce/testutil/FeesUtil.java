package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;

@Component
public class FeesUtil {

    private static final WireMockServer FEES_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

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

    @Autowired
    private ObjectMapper objectMapper;

    public void stubForFeesLookup() throws JsonProcessingException {
        FEES_SERVER.stubFor(get("/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce"
            ).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getFeeResponse())))
        );
    }

    public void stubForFeesNotFound() {
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
