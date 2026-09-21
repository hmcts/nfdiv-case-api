package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JacksonConfigurationVerifier {

    @Autowired
    public JacksonConfigurationVerifier(ObjectMapper objectMapper) {
        if (objectMapper.version().getMajorVersion() != 2) {
            throw new IllegalStateException("The application ObjectMapper must be Jackson 2 for CCD wire contracts.");
        }
        if (objectMapper.getFactory().isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
            throw new IllegalStateException("Jackson ObjectMapper is configured with AUTO_CLOSE_JSON_CONTENT enabled. "
                + "This can cause issues with streaming JSON responses an must be disabled.");
        }
    }
}
