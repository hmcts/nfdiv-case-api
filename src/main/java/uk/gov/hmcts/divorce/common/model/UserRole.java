package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonValue;
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
    CASEWORKER_DIVORCE_SYSTEMUPDATE("caseworker-divorce-systemupdate", "CRU"),
    APPLICANT_2_SOLICITOR("[APP2SOLICITOR]", "CRU"),
    APPLICANT_1_SOLICITOR("[APP1SOLICITOR]", "CRU"),
    CREATOR("[CREATOR]", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}
