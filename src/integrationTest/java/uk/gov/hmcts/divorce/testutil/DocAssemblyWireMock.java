package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

public final class DocAssemblyWireMock {

    private static final WireMockServer DOC_ASSEMBLY_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private DocAssemblyWireMock() {
    }

    public static void start() {
        if (!DOC_ASSEMBLY_SERVER.isRunning()) {
            DOC_ASSEMBLY_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (DOC_ASSEMBLY_SERVER.isRunning()) {
            DOC_ASSEMBLY_SERVER.stop();
            DOC_ASSEMBLY_SERVER.resetAll();
        }
    }

    public static void stubForDocAssembly() throws IOException {
        String expectedResponse = expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")
            .replace("<docUuid>", "8d2bd0f2-80e9-4b0f-b38d-2c138b243e27")
            .replace("<templateId>", "RGl2b3JjZV9DUF9NaW5pX0RyYWZ0X1BldGl0aW9uX1RlbXBsYXRlLmRvY3g=");

        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedResponse))
        );
    }

    public static void stubForDocAssemblyWith(String uuid, String templateId) throws IOException {
        String encodedTemplateId = Base64.getEncoder().encodeToString(templateId.getBytes());

        String expectedResponse = expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")
            .replace("<docUuid>", uuid)
            .replace("<templateId>", templateId);

        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
                .withRequestBody(containing(encodedTemplateId))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedResponse))
        );
    }

    public static void stubForDocAssemblyUnauthorized() throws IOException {
        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
                .willReturn(unauthorized()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
        );
    }

    private static String expectedDocAssemblyResponse(final String resourcePath) throws IOException {
        return new String(readAllBytes(getFile(resourcePath).toPath()));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("doc_assembly.url=" + "http://localhost:" + DOC_ASSEMBLY_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
