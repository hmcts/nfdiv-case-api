package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

public final class SendLetterWireMock {

    private static final WireMockServer SEND_LETTER_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private SendLetterWireMock() {
    }

    public static void start() {
        if (!SEND_LETTER_SERVER.isRunning()) {
            SEND_LETTER_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (SEND_LETTER_SERVER.isRunning()) {
            SEND_LETTER_SERVER.stop();
            SEND_LETTER_SERVER.resetAll();
        }
    }

    public static void stubSendLetters(final String sendLetterResponse) {
        SEND_LETTER_SERVER.stubFor(post("/letters?isAsync=true")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withBody(sendLetterResponse)
            ));
    }

    public static void stubStatusOfSendLetter(final UUID letterId, final String letterStatus) {
        SEND_LETTER_SERVER.stubFor(get("/letters/" + letterId + "?include-additional-info=false&check-duplicate=true")
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withBody(letterStatus)
            ));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("send-letter.url=" + "http://localhost:" + SEND_LETTER_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
