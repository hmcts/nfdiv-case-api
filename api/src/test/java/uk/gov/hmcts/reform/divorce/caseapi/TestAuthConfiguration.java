package uk.gov.hmcts.reform.divorce.caseapi;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.CCD_DATA;

@Configuration
public class TestAuthConfiguration {
    @Bean
    @ConditionalOnProperty(name = "s2s.stub", havingValue = "true", matchIfMissing = true)
    public AuthTokenValidator tokenValidatorStub() {
        return new AuthTokenValidator() {
            public void validate(String token) {
                throw new NotImplementedException();
            }

            public void validate(String token, List<String> roles) {
                throw new NotImplementedException();
            }

            public String getServiceName(String token) {
                return CCD_DATA;
            }
        };
    }
}
