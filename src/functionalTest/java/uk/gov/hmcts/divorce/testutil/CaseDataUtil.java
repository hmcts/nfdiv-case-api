package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.springframework.util.ResourceUtils.getFile;

public final class CaseDataUtil {

    private static final CaseDataUtil.MapTypeReference MAP_TYPE = new CaseDataUtil.MapTypeReference();

    private CaseDataUtil() {
    }

    public static Map<String, Object> caseData(final String resourcePath) throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        final File jsonFile = getFile(resourcePath);

        return objectMapper.readValue(jsonFile, MAP_TYPE);
    }

    private static class MapTypeReference extends TypeReference<Map<String, Object>> {
    }
}
