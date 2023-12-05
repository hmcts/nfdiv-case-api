package uk.gov.hmcts.divorce.idam;


import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Getter
@AllArgsConstructor
public class User {
    private String authToken;
    private UserInfo userDetails;
}
