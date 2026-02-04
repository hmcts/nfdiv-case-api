package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRolesForAccessProfiles implements HasRole {

    CASE_WORKER("idam:caseworker-divorce-courtadmin_beta", "CRU"),
    CASE_WORKER_BULK_SCAN("idam:caseworker-divorce-bulkscan", "CRU"),
    LEGAL_ADVISOR("idam:caseworker-divorce-courtadmin-la", "CRU"),
    SUPER_USER("idam:caseworker-divorce-superuser", "CRU"),
    SYSTEMUPDATE("idam:caseworker-divorce-systemupdate", "CRUD"),
    JUDGE("idam:caseworker-divorce-judge", "CRU"),
    NOC_APPROVER("idam:caseworker-approver", "CRU"),
    RPA_ROBOT("idam:caseworker-divorce-rparobot", "CRU"),
    TTL_MANAGER("idam:cft-ttl-manager", "CRU"),

    SOLICITOR("idam:caseworker-divorce-solicitor", "CRU"),
    APPLICANT_1_SOLICITOR("[APPONESOLICITOR]", "CRU"),
    APPLICANT_2_SOLICITOR("[APPTWOSOLICITOR]", "CRU"),
    ORGANISATION_CASE_ACCESS_ADMINISTRATOR("idam:caseworker-caa", "CRU"),

    CITIZEN("idam:citizen", "CRU"),
    CREATOR("[CREATOR]", "CRU"),
    APPLICANT_2("[APPLICANTTWO]", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
