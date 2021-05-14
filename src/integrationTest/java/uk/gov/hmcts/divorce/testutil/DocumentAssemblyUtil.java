package uk.gov.hmcts.divorce.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;

@Component
public class DocumentAssemblyUtil {

    private static final WireMockServer DOC_ASSEMBLY_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

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

    public void stubForDocAssembly() throws IOException {
        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTHORIZATION))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")))
        );
    }

    private String expectedDocAssemblyResponse(final String resourcePath) throws IOException {
        File jsonFile = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(jsonFile.toPath()));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "doc_assembly.url=" + "http://localhost:" + DOC_ASSEMBLY_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
