package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

public final class PrdOrganisationWireMock {

    private static final WireMockServer PRD_ORGANISATION_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private PrdOrganisationWireMock() {
    }

    public static void start() {
        if (!PRD_ORGANISATION_SERVER.isRunning()) {
            PRD_ORGANISATION_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (PRD_ORGANISATION_SERVER.isRunning()) {
            PRD_ORGANISATION_SERVER.stop();
            PRD_ORGANISATION_SERVER.resetAll();
        }
    }

    public static void stubGetOrganisationEndpoint(final String organisationResponse) {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(organisationResponse)
            )
        );
    }

    public static void stubGetOrganisationByEmailEndpoint(final String organisationResponse) {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations/pbas")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(organisationResponse)
            )
        );
    }

    public static void stubGetOrganisationEndpointForFailure() {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            )
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("prd.api.url=" + "http://localhost:" + PRD_ORGANISATION_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
