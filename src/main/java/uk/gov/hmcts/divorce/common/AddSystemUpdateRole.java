package uk.gov.hmcts.divorce.common;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;

@Component
public class AddSystemUpdateRole {

    private static final String ENVIRONMENT_AAT = "aat";

    public List<UserRole> addIfConfiguredForEnvironment(List<UserRole> userRoles) {
        List<UserRole> existingRoles = new ArrayList<>(userRoles);
        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);

        if (null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT)) {
            existingRoles.add(SYSTEMUPDATE);
        }

        return existingRoles;
    }

    public boolean isEnvironmentAat() {
        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);
        return null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT);
    }
}
