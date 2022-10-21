package uk.gov.hmcts.divorce.testutil;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    public String generateIdamTokenForSolicitor() {
        return tokensMap.computeIfAbsent(solicitorUsername, token -> idamClient.getAccessToken(solicitorUsername, solicitorPassword));
    }

    public String generateIdamTokenForSystem() {
        String systemUserToken = cache.getIfPresent(systemUpdateUsername);
        if (systemUserToken == null) {
            systemUserToken = idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
            cache.put(systemUpdateUsername, systemUserToken);
        }
        return systemUserToken;
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
