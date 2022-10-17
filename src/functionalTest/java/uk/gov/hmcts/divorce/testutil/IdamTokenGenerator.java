package uk.gov.hmcts.divorce.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TestPropertySource("classpath:application.yaml")
@Service
public class IdamTokenGenerator {

    private final Map<String, String> tokensMap = new ConcurrentHashMap<>();

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

    public String generateIdamTokenForSolicitor() {
        return tokensMap.computeIfAbsent(solicitorUsername, token -> idamClient.getAccessToken(solicitorUsername, solicitorPassword));
    }

    public String generateIdamTokenForSystem() {
        return tokensMap.computeIfAbsent(
            systemUpdateUsername,
            token -> idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword)
        );
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
