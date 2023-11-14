package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;

public final class CdamWireMock {

    private static final WireMockServer CDAM_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private CdamWireMock() {
    }

    public static void start() {
        if (!CDAM_SERVER.isRunning()) {
            CDAM_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (CDAM_SERVER.isRunning()) {
            CDAM_SERVER.stop();
            CDAM_SERVER.resetAll();
        }
    }

    public static void stubCdamDeleteWith(final String documentUuid, final HttpStatus httpStatus) {
        CDAM_SERVER.stubFor(delete("/cases/documents/" + documentUuid + "?permanent=true")
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + TEST_SYSTEM_AUTHORISATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse().withStatus(httpStatus.value())));
    }

    public static void stubCdamDownloadBinaryWith(final String documentUuid, final byte[] documentBytes) {
        CDAM_SERVER.stubFor(get("/cases/documents/" + documentUuid + "/binary")
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + TEST_SYSTEM_AUTHORISATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(documentBytes)
            ));
    }

    public static void stubCdamUploadWith(final String documentUuid, final String label) {
        CDAM_SERVER.stubFor(post("/cases/documents")
            .andMatching(request -> MatchResult.of(request.getBodyAsString().contains(label)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.CREATED.value())
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                            "documents": [
                                {
                                    "_links": {
                                        "self": { "href": "http://dm-store-aat.service.core-compute-aat.internal/documents/%s" },
                                        "binary": { "href": "http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary" }
                                    },
                                    "originalDocumentName": "%s"
                                }
                            ]
                        }
                        """.formatted(documentUuid, documentUuid, label + ".pdf")
                    )
            ));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("case_document_am.url=" + "http://localhost:" + CDAM_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
