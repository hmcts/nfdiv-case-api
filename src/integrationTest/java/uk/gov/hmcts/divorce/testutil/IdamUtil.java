package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@Component
public class IdamUtil {

    public static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";
    public static final String CASEWORKER_ROLE = "caseworker-divorce";
    private static final WireMockServer IDAM_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    public static void start() {
        if (!IDAM_SERVER.isRunning()) {
            IDAM_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (IDAM_SERVER.isRunning()) {
            IDAM_SERVER.stop();
            IDAM_SERVER.resetAll();
        }
    }

    public void stubForIdamDetails(final String testAuthorizationToken,
                                   final String solicitorUserId,
                                   final String solicitorRole) {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + testAuthorizationToken))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(getUserDetailsForRole(solicitorUserId, solicitorRole)))
        );
    }

    public void stubForIdamToken() throws UnsupportedEncodingException {
        IDAM_SERVER.stubFor(post("/o/token")
            .withRequestBody(new EqualToPattern(tokenBody()))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{ \"access_token\" : \"" + CASEWORKER_AUTH_TOKEN + "\" }")))
        ;
    }

    public void stubForIdamFailure() {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("Invalid idam credentials"))
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "idam.api.url=" + "http://localhost:" + IDAM_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    private static String getUserDetailsForRole(String userId, String role) {
        return "{\"id\":\"" + userId
            + "\",\"email\":\"" + TEST_USER_EMAIL
            + "\",\"forename\":\"forename\",\"surname\":\"Surname\",\"roles\":[\"" + role + "\"]}";
    }

    private static String tokenBody() throws UnsupportedEncodingException {
        return "password=dummy"
            + "&grant_type=password"
            + "&scope=" + encode("openid profile roles", UTF_8.name())
            + "&client_secret=BBBBBBBBBBBBBBBB"
            + "&redirect_uri=" + encode("http://localhost:3001/oauth2/callback", UTF_8.name())
            + "&client_id=divorce"
            + "&username=" + encode("dummycaseworker@test.com", UTF_8.name());
    }
}
