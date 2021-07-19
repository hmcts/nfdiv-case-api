package uk.gov.hmcts.divorce.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@TestPropertySource("classpath:application.yaml")
@Service
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Autowired
    private IdamClient idamClient;

    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken("TEST_SOLICITOR@mailinator.com", "genericPassword123");
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
