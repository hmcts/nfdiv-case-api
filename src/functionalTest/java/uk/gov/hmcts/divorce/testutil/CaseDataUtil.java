package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static org.springframework.util.ResourceUtils.getFile;

public final class CaseDataUtil {

    private static final CaseDataUtil.MapTypeReference MAP_TYPE = new CaseDataUtil.MapTypeReference();

    private CaseDataUtil() {
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    public static Map<String, Object> caseData(final String resourcePath) throws IOException {
        return getObjectMapper().readValue(getFile(resourcePath), MAP_TYPE);
    }

    public static Map<String, Object> caseDataFromString(final String caseDataString) throws IOException {
        return getObjectMapper().readValue(caseDataString, MAP_TYPE);
    }

    private static class MapTypeReference extends TypeReference<Map<String, Object>> {
    }
}
