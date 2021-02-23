package uk.gov.hmcts.reform.ccd.ccd.model;

import uk.gov.hmcts.ccd.sdk.types.HasRole;

public enum UserRole implements HasRole {

    CITIZEN("citizen");

    private final String roleName;

    UserRole(final String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getRole() {
        return this.roleName;
    }
}
