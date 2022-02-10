package uk.gov.hmcts.divorce.bulkscan.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FileUtil {

    private FileUtil() {
    }

    public static String loadJson(String jsonFilePath) throws IOException {
        return Files.readString(Paths.get(jsonFilePath), UTF_8);
    }
}
