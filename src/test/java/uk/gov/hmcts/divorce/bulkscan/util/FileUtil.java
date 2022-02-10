package uk.gov.hmcts.divorce.bulkscan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FileUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(LocalDateSerializer.INSTANCE);
        MAPPER.registerModule(datetime);
    }

    private FileUtil() {
    }

    public static String loadJson(String jsonFilePath) throws IOException {
        return Files.readString(Paths.get(jsonFilePath), UTF_8);
    }

    public static <T> T jsonToObject(String s, Class<T> clazz) throws IOException {
        return MAPPER.readValue(new File(s), clazz);
    }
}
