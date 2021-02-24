package uk.gov.hmcts.reform.divorce.ccd.model;

import uk.gov.hmcts.ccd.sdk.types.HasRole;

public enum UserRole implements HasRole {

    CASEWORKER_DIVORCE_COURTADMIN_BETA("caseworker-divorce-courtadmin_beta"),
    CASEWORKER_DIVORCE_COURTADMIN("caseworker-divorce-courtadmin"),
    CITIZEN("citizen"),
    CASEWORKER_DIVORCE_SOLICITOR("caseworker-divorce-solicitor"),
    CASEWORKER_DIVORCE_SUPERUSER("caseworker-divorce-superuser"),
    CASEWORKER_DIVORCE_COURTADMIN_LA("caseworker-divorce-courtadmin-la");

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
