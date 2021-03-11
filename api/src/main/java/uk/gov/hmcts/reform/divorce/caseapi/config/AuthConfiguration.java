package uk.gov.hmcts.reform.divorce.caseapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

@Configuration
public class AuthConfiguration {
    @Bean
    public AuthTokenValidator tokenValidator(ServiceAuthorisationApi s2sApi) {
        return new ServiceAuthTokenValidator(s2sApi);
    }
}
