package uk.gov.hmcts.divorce.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@TestPropertySource("classpath:application.yaml")
@Service
@EnableCaching
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Value("${idam.systemupdate.username}")
    private String systemUpdateUsername;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Autowired
    private IdamClient idamClient;

    @Cacheable(value = "solicitorToken", key = "#root.methodName")
    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }

    @Cacheable(value = "systemToken", key = "#root.methodName")
    public String generateIdamTokenForSystem() {
        return idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
