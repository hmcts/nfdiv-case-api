package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.file.Files.readAllBytes;
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
        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")))
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
