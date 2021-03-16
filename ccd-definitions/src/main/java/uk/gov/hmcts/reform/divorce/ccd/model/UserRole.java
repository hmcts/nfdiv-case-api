package uk.gov.hmcts.reform.divorce.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    CASEWORKER_DIVORCE_COURTADMIN_BETA("caseworker-divorce-courtadmin_beta", "CRU"),
    CASEWORKER_DIVORCE_COURTADMIN("caseworker-divorce-courtadmin", "CRU"),
    CITIZEN("citizen", "CRU"),
    CASEWORKER_DIVORCE_SOLICITOR("caseworker-divorce-solicitor", "CRU"),
    CASEWORKER_DIVORCE_SUPERUSER("caseworker-divorce-superuser", "CRU"),
    CASEWORKER_DIVORCE_COURTADMIN_LA("caseworker-divorce-courtadmin-la", "CRU"),
    CASEWORKER_DIVORCE_SYSTEMUPDATE("caseworker-divorce-systemupdate", "CRU");

    private final String role;
    private final String caseTypePermissions;

}
