package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    CASEWORKER_COURTADMIN("caseworker-divorce-courtadmin_beta", "CRU"),
    CASEWORKER_LEGAL_ADVISOR("caseworker-divorce-courtadmin-la", "CRU"),
    CASEWORKER_SUPERUSER("caseworker-divorce-superuser", "CRU"),
    CASEWORKER_SYSTEMUPDATE("caseworker-divorce-systemupdate", "CRU"),

    SOLICITOR("caseworker-divorce-solicitor", "CRU"),
    APPLICANT_1_SOLICITOR("[APPONESOLICITOR]", "CRU"),
    APPLICANT_2_SOLICITOR("[APPTWOSOLICITOR]", "CRU"),
    ORGANISATION_CASE_ACCESS_ADMINISTRATOR("caseworker-caa", "CRU"),

    CITIZEN("citizen", "CRU"),
    CREATOR("[CREATOR]", "CRU"),
    APPLICANT_2("[APPLICANTTWO]", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}
