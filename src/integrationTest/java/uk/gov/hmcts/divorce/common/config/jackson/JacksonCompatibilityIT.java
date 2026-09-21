package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class JacksonCompatibilityIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Test
    @SuppressWarnings("removal")
    void mvcUsesJackson2ForApplicationResponses() throws Exception {
        List<HttpMessageConverter<?>> jacksonConverters = requestMappingHandlerAdapter
            .getMessageConverters()
            .stream()
            .filter(converter -> converter.getClass().getName().contains("Jackson"))
            .toList();

        List<MappingJackson2HttpMessageConverter> jackson2Converters = requestMappingHandlerAdapter
            .getMessageConverters()
            .stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .toList();

        assertThat(objectMapper.getClass().getName()).startsWith("com.fasterxml.jackson.");
        assertThat(objectMapper.writeValueAsString(AddressGlobalUK.builder()
            .addressLine1("line 1")
            .postTown("town")
            .postCode("postcode")
            .country("UK")
            .build()))
            .contains("\"AddressLine1\":\"line 1\"")
            .contains("\"PostTown\":\"town\"")
            .contains("\"PostCode\":\"postcode\"")
            .contains("\"Country\":\"UK\"");
        assertThat(jacksonConverters)
            .as("Jackson converters used by MVC")
            .first()
            .isInstanceOf(MappingJackson2HttpMessageConverter.class);
        assertThat(jackson2Converters)
            .extracting(MappingJackson2HttpMessageConverter::getObjectMapper)
            .contains(objectMapper);
    }
}
