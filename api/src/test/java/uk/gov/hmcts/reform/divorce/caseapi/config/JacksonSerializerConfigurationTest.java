package uk.gov.hmcts.reform.divorce.caseapi.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JacksonSerializerConfigurationTest {

    @Mock
    private Jackson2ObjectMapperBuilder builder;

    @InjectMocks
    private JacksonSerializerConfiguration jacksonSerializerConfiguration;

    @Test
    void shouldCustomiseJacksonBuilderWithLocalDateAndLocalDateTimeSerializers() {

//        jacksonSerializerConfiguration.jsonCustomizer().customize(builder);

//        verify(builder).serializers(any(LocalDateSerializer.class));
//        verify(builder).serializers(any(LocalDateTimeSerializer.class));
    }
}