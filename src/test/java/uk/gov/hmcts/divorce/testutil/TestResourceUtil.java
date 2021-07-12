package uk.gov.hmcts.divorce.testutil;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class TestResourceUtil {

    private TestResourceUtil() {

    }

    public static String expectedResponse(final String resourcePath) throws IOException {
        return resourceAsString(resourcePath);
    }

    public static String resourceAsString(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static byte[] resourceAsBytes(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return Files.readAllBytes(file.toPath());
    }
}
