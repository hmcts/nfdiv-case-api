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
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

public final class CaseDataUtil {

    public static final WireMockServer CASE_DATA_SERVER = new WireMockServer(wireMockConfig().dynamicPort());
    public static final String CASE_WORKER_TOKEN = "test-caseworker-token";
    public static final String SERVICE_AUTH_TOKEN = "test-service-auth-token";

    private CaseDataUtil() {
    }

    public static void stubForCcdCaseRoles() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + CASE_WORKER_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[APPONESOLICITOR]\"]}",
                true,
                true)
            )
            .willReturn(aResponse().withStatus(200))
        );
    }

    public static void stubForCcdCaseRolesUpdateFailure() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + CASE_WORKER_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[APPONESOLICITOR]\"]}",
                true,
                true)
            )
            .willReturn(aResponse().withStatus(403))
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "core_case_data.api.url=" + "http://localhost:" + CASE_DATA_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
