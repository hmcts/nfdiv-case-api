package uk.gov.hmcts.divorce.idam;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.divorce.common.config.ControllerConstants.BEARER_PREFIX;

@Service
@Slf4j
public class IdamService {
    @Value("${idam.systemupdate.username}")
    private String systemUpdateUserName;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Value("${idam.divorce.username}")
    private String oldDivorceUserName;

    @Value("${idam.divorce.password}")
    private String oldDivorcePassword;

    @Autowired
    private IdamClient idamClient;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    public User retrieveUser(String authorisation) {
        final String bearerToken = getBearerToken(authorisation);
        final var userDetails = idamClient.getUserInfo(bearerToken);

        return new User(bearerToken, userDetails);
    }

    public User retrieveSystemUpdateUserDetails() {
        return retrieveUser(getCachedIdamOauth2Token(systemUpdateUserName, systemUpdatePassword));
    }

    public User retrieveOldSystemUpdateUserDetails() {
        User user = null;

        try {
            user = retrieveUser(getCachedIdamOauth2Token(oldDivorceUserName, oldDivorcePassword));
        } catch (FeignException e) {
            log.info("Exception in retrieveOldSystemUpdateUserDetails {} for user:{}", e.getCause(), oldDivorceUserName);
        }
        return user;
    }

    private String getCachedIdamOauth2Token(String username, String password) {
        String userToken = cache.getIfPresent(username);
        if (userToken == null) {
            userToken = idamClient.getAccessToken(username, password);
            cache.put(username, userToken);
        }
        return userToken;
    }

    private String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }
        return token.startsWith(BEARER_PREFIX) ? token : BEARER_PREFIX.concat(token);
    }
}
