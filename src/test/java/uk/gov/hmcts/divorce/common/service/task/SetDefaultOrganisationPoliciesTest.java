package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;

class SetDefaultOrganisationPoliciesTest {
    private SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    @BeforeEach
    void setUp() {
        setDefaultOrganisationPolicies = new SetDefaultOrganisationPolicies();
    }

    @Test
    void setsDefaultOrganisationPoliciesForUnrepresentedApplicants() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();

        setDefaultOrganisationPolicies.apply(caseDetails);

        final OrganisationPolicy<UserRole> app1OrgPolicy = caseData.getApplicant1().getSolicitor().getOrganisationPolicy();
        final OrganisationPolicy<UserRole> app2OrgPolicy = caseData.getApplicant2().getSolicitor().getOrganisationPolicy();

        assertAll(
            () -> assertEquals(null, app1OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(null, app1OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_1_SOLICITOR, app1OrgPolicy.getOrgPolicyCaseAssignedRole()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_2_SOLICITOR, app2OrgPolicy.getOrgPolicyCaseAssignedRole())
        );
    }

    @Test
    void setsMissingCaseAssignedRoleFieldForRepresentedApplicants() {
        Solicitor applicant1Solicitor = Solicitor.builder().organisationPolicy(organisationPolicyWithoutCaseAssignedRole()).build();
        Applicant applicant1 = Applicant.builder().solicitor(applicant1Solicitor).build();

        final CaseData caseData = CaseData.builder().applicant1(applicant1).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();


        setDefaultOrganisationPolicies.apply(caseDetails);

        final OrganisationPolicy app1OrgPolicy = caseData.getApplicant1().getSolicitor().getOrganisationPolicy();
        final OrganisationPolicy app2OrgPolicy = caseData.getApplicant2().getSolicitor().getOrganisationPolicy();

        assertAll(
            () -> assertEquals(TEST_ORG_NAME, app1OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(TEST_ORG_ID, app1OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_1_SOLICITOR, app1OrgPolicy.getOrgPolicyCaseAssignedRole()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_2_SOLICITOR, app2OrgPolicy.getOrgPolicyCaseAssignedRole())
        );
    }

    @Test
    void doesNotOverrideOrganisationDetailsForRepresentedApplicants() {
        Solicitor applicant1Solicitor = Solicitor.builder().organisationPolicy(organisationPolicy()).build();
        Applicant applicant1 = Applicant.builder().solicitor(applicant1Solicitor).build();

        final CaseData caseData = CaseData.builder().applicant1(applicant1).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();


        setDefaultOrganisationPolicies.apply(caseDetails);

        final OrganisationPolicy app1OrgPolicy = caseData.getApplicant1().getSolicitor().getOrganisationPolicy();
        final OrganisationPolicy app2OrgPolicy = caseData.getApplicant2().getSolicitor().getOrganisationPolicy();

        assertAll(
            () -> assertEquals(TEST_ORG_NAME, app1OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(TEST_ORG_ID, app1OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_2_SOLICITOR, app1OrgPolicy.getOrgPolicyCaseAssignedRole()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationName()),
            () -> assertEquals(null, app2OrgPolicy.getOrganisation().getOrganisationId()),
            () -> assertEquals(UserRole.APPLICANT_2_SOLICITOR, app2OrgPolicy.getOrgPolicyCaseAssignedRole())
        );
    }

    private static OrganisationPolicy<UserRole> organisationPolicy() {
        return OrganisationPolicy.<UserRole>builder()
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .organisation(Organisation
                .builder()
                .organisationName(TEST_ORG_NAME)
                .organisationId(TEST_ORG_ID)
                .build())
            .build();
    }

    private static OrganisationPolicy<UserRole> organisationPolicyWithoutCaseAssignedRole() {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationName(TEST_ORG_NAME)
                .organisationId(TEST_ORG_ID)
                .build())
            .build();
    }
}
