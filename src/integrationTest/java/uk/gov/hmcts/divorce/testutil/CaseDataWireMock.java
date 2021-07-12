package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.hmcts.divorce.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
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

    public static void stubForCcdCaseRoles() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + SYSTEM_UPDATE_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTHORIZATION))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[APPONESOLICITOR]\"]}",
                true,
                true))
            .willReturn(aResponse().withStatus(200))
        );
    }

    public static void stubForCitizenCcdCaseRoles() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTHORIZATION))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"3\", \"case_roles\":[\"[APPLICANTTWO]\"]}",
                true,
                true))
            .willReturn(aResponse().withStatus(200))
        );
    }

    public static void stubForCcdCaseRolesUpdateFailure() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + CASEWORKER_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTHORIZATION))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[APPONESOLICITOR]\"]}",
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
