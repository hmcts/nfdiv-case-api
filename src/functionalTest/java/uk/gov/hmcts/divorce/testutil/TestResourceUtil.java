package uk.gov.hmcts.divorce.testutil;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class TestResourceUtil {
    public static final String ABOUT_TO_START_CALLBACK_URL = "/callbacks/about-to-start";
    public static final String ABOUT_TO_SUBMIT_CALLBACK_URL = "/callbacks/about-to-submit";
    public static final String SUBMITTED_CALLBACK_URL = "/callbacks/submitted";
    public static final String MID_EVENT_CALLBACK_ABOUT_THE_SOL_URL = "/callbacks/mid-event?page=SolAboutTheSolicitor";

    private TestResourceUtil() {

    }

    public static String expectedCcdCallbackResponse(String resourcePath) throws IOException {
        File jsonFile = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(jsonFile.toPath()));
    }
}
