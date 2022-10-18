package uk.gov.hmcts.divorce.idam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.divorce.common.config.ControllerConstants.BEARER_PREFIX;

@Service
public class IdamService {
    @Value("${idam.systemupdate.username}")
    private String systemUpdateUserName;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Autowired
    private IdamClient idamClient;

    private final Map<String, String> tokensMap = new ConcurrentHashMap<>();

    public User retrieveUser(String authorisation) {
        final String bearerToken = getBearerToken(authorisation);
        final UserDetails userDetails = idamClient.getUserDetails(bearerToken);

        return new User(bearerToken, userDetails);
    }

    public User retrieveSystemUpdateUserDetails() {
        String clusterName = System.getenv().getOrDefault("CLUSTER_NAME", null);

        if (null != clusterName && !clusterName.contains("prod")) {
            retrieveUser(getCachedIdamOauth2Token(systemUpdateUserName, systemUpdatePassword));
        }

        return retrieveUser(getIdamOauth2Token(systemUpdateUserName, systemUpdatePassword));
    }

    private String getCachedIdamOauth2Token(String username, String password) {
        return tokensMap.computeIfAbsent(username, token -> idamClient.getAccessToken(username, password));
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    private String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }
        return token.startsWith(BEARER_PREFIX) ? token : BEARER_PREFIX.concat(token);
    }
}
