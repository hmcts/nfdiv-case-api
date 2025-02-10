package uk.gov.hmcts.divorce.divorcecase.model.access;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRolesForAccessProfiles;

@Component
public class RoleToAccessProfiles implements CCDConfig<CaseData, State, UserRolesForAccessProfiles> {
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRolesForAccessProfiles> configBuilder) {
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CASE_WORKER)
                .accessProfiles("caseworker-divorce-courtadmin_beta").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CASE_WORKER_BULK_SCAN)
                .accessProfiles("caseworker-divorce-bulkscan").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.LEGAL_ADVISOR)
                .accessProfiles("caseworker-divorce-courtadmin-la").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SUPER_USER)
                .accessProfiles("caseworker-divorce-superuser").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SYSTEMUPDATE)
                .accessProfiles("caseworker-divorce-systemupdate").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.JUDGE)
                .accessProfiles("caseworker-divorce-judge").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.NOC_APPROVER)
                .accessProfiles("caseworker-approver").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RPA_ROBOT)
                .accessProfiles("caseworker-divorce-rparobot").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.TTL_MANAGER)
                .accessProfiles("TTL_profile").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SOLICITOR)
                .accessProfiles("caseworker-divorce-solicitor").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.APPLICANT_1_SOLICITOR)
                .accessProfiles("[APPONESOLICITOR]").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.APPLICANT_2_SOLICITOR)
                .accessProfiles("[APPTWOSOLICITOR]").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ORGANISATION_CASE_ACCESS_ADMINISTRATOR)
                .accessProfiles("caseworker-caa").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CITIZEN)
                .accessProfiles("citizen").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CREATOR)
                .accessProfiles("[CREATOR]").build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.APPLICANT_2)
                .accessProfiles("[APPLICANTTWO]").build();
    }
}
