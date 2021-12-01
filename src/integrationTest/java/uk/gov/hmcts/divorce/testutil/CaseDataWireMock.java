package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;

public final class CaseDataWireMock {

    private static final WireMockServer CASE_DATA_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private CaseDataWireMock() {
    }

    public static void start() {
        if (!CASE_DATA_SERVER.isRunning()) {
            CASE_DATA_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (CASE_DATA_SERVER.isRunning()) {
            CASE_DATA_SERVER.stop();
            CASE_DATA_SERVER.resetAll();
        }
    }

    public static void stubForCaseAssignmentRoles() {
        CASE_DATA_SERVER.stubFor(delete(urlMatching("/case-users"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + SYSTEM_UPDATE_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(AUTH_HEADER_VALUE))
            .withRequestBody(new EqualToJsonPattern(
                "{\"case_users\":[{\"case_id\":\"1616591401473378\",\"case_role\":\"[CREATOR]\",\"organisation_id\":\"ABC123\","
                    + "\"user_id\":\"1\"}]}",
                true,
                true))
            .willReturn(aResponse().withStatus(200))
        );

        CASE_DATA_SERVER.stubFor(post(urlMatching("/case-users"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + SYSTEM_UPDATE_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(AUTH_HEADER_VALUE))
            .withRequestBody(new EqualToJsonPattern(
                "{\"case_users\":[{\"case_id\":\"1616591401473378\",\"case_role\":\"[APPONESOLICITOR]\",\"organisation_id\":\"ABC123\","
                    + "\"user_id\":\"1\"}]}",
                true,
                true))
            .willReturn(aResponse().withStatus(200))
        );
    }

    public static void stubForCitizenCcdCaseRoles() {
        CASE_DATA_SERVER.stubFor(post(urlMatching("/case-users"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTHORIZATION))
            .withRequestBody(new EqualToJsonPattern(
                "{ \"case_users\": [{\"user_id\" : \"3\", \"case_id\" : \"1616591401473378\", \"case_role\":\"[APPLICANTTWO]\"}]}",
                true,
                true))
            .willReturn(aResponse().withStatus(200))
        );
    }

    public static void stubForCcdCaseRolesUpdateFailure() {
        CASE_DATA_SERVER.stubFor(delete(urlMatching("/case-users"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + SYSTEM_UPDATE_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(AUTH_HEADER_VALUE))
            .withRequestBody(new EqualToJsonPattern(
                "{\"case_users\":[{\"case_id\":\"1616591401473378\",\"case_role\":\"[CREATOR]\",\"organisation_id\":\"ABC123\","
                    + "\"user_id\":\"1\"}]}",
                true,
                true))
            .willReturn(aResponse().withStatus(403))
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("core_case_data.api.url=" + "http://localhost:" + CASE_DATA_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
