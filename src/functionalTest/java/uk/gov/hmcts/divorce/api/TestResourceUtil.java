package uk.gov.hmcts.divorce.api;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class TestResourceUtil {
    public static final String ABOUT_TO_START_CALLBACK_URL = "/callbacks/about-to-start";
    public static final String ABOUT_TO_SUBMIT_CALLBACK_URL = "/callbacks/about-to-submit";

    private TestResourceUtil() {

    }

    public static String expectedCcdCallbackResponse(String resourcePath) throws IOException {
        File issueFeesResponseJsonFile = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(issueFeesResponseJsonFile.toPath()));
    }
}
