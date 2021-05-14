package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Component
public class PrdOrganisationUtil {

    private static final WireMockServer PRD_ORGANISATION_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

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

    @Autowired
    private ObjectMapper objectMapper;

    public void stubGetOrganisationEndpoint(final String organisationId) throws JsonProcessingException {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(
                    OrganisationsResponse
                        .builder()
                        .organisationIdentifier(organisationId)
                        .build()
                    )
                )
            )
        );
    }

    public void stubGetOrganisationEndpointForFailure() {
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
            TestPropertyValues.of(
                "prd.api.url=" + "http://localhost:" + PRD_ORGANISATION_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
