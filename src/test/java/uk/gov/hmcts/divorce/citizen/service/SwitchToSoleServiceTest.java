package uk.gov.hmcts.divorce.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class SwitchToSoleServiceTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SwitchToSoleService switchToSoleService;

    @Test
    void shouldSwitchUserDataIfApplicant2TriggeredD84SwitchToSole() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        caseData.setFinalOrder(FinalOrder.builder().build());

        final Applicant applicant1BeforeSwitch = caseData.getApplicant1();
        final Applicant applicant2BeforeSwitch = caseData.getApplicant2();

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplicant1()).isEqualTo(applicant2BeforeSwitch);
        assertThat(caseData.getApplicant2()).isEqualTo(applicant1BeforeSwitch);
    }

    @Test
    void shouldSwitchDocumentsUploadedIfApplicant2TriggeredD84SwitchToSole() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        caseData.setFinalOrder(FinalOrder.builder().build());

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);
        caseData.getDocuments().setApplicant1DocumentsUploaded(singletonList(doc1));

        final ListValue<DivorceDocument> doc2 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "co_application.pdf", CONDITIONAL_ORDER_APPLICATION);
        caseData.getDocuments().setApplicant2DocumentsUploaded(singletonList(doc2));

        final List<ListValue<DivorceDocument>> applicant1DocumentsBeforeSwitch = caseData.getDocuments().getApplicant1DocumentsUploaded();
        final List<ListValue<DivorceDocument>> applicant2DocumentsBeforeSwitch = caseData.getDocuments().getApplicant2DocumentsUploaded();

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getDocuments().getApplicant1DocumentsUploaded()).isEqualTo(applicant2DocumentsBeforeSwitch);
        assertThat(caseData.getDocuments().getApplicant2DocumentsUploaded()).isEqualTo(applicant1DocumentsBeforeSwitch);
    }

    @Test
    void shouldSetDivorceWhoIfNewApplicant2GenderIsNotNull() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        caseData.setFinalOrder(FinalOrder.builder().build());

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplication().getDivorceWho()).isNotNull();
        assertThat(caseData.getApplication().getDivorceWho()).isEqualTo(WIFE);
    }

    @Test
    void shouldSetDivorceWhoToNullIfNewApplicant2GenderIsNull() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        caseData.setFinalOrder(FinalOrder.builder().build());
        caseData.getApplicant1().setGender(null);

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplication().getDivorceWho()).isNull();
    }

    @Test
    void shouldSetCorrectOrgPolicyCaseAssignedRolesWhenApplicantDataIsSwitched() {
        CaseData caseData = validJointApplicant1CaseData();
        Solicitor app1Solicitor = new Solicitor().builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR).build())
            .build();
        caseData.getApplicant1().setSolicitor(app1Solicitor);

        Solicitor app2Solicitor = new Solicitor().builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().orgPolicyCaseAssignedRole(APPLICANT_2_SOLICITOR).build())
            .build();
        caseData.getApplicant2().setSolicitor(app2Solicitor);

        switchToSoleService.switchApplicantData(caseData);

        OrganisationPolicy<UserRole> newApp1OrgPolicy = caseData.getApplicant1().getSolicitor().getOrganisationPolicy();
        OrganisationPolicy<UserRole> newApp2OrgPolicy = caseData.getApplicant2().getSolicitor().getOrganisationPolicy();
        assertThat(newApp1OrgPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(APPLICANT_1_SOLICITOR);
        assertThat(newApp2OrgPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(APPLICANT_2_SOLICITOR);
    }

    @Test
    void shouldSwitchCitizenUserRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder().build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", CREATOR))
            .thenReturn(getCaseAssignmentRequest("2", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("1", UserRole.APPLICANT_2));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", CREATOR))
            .thenReturn(getCaseAssignmentRequest("1", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("2", UserRole.APPLICANT_2));

        switchToSoleService.switchUserRoles(caseData, caseId);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", CREATOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", UserRole.APPLICANT_2)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", CREATOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", UserRole.APPLICANT_2)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
    }

    @Test
    void shouldSwitchSolicitorUserRolesIfApplicant2SolicitorTriggeredSwitchToSoleCo() {
        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().solicitorRepresented(YES).build())
            .build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPTWOSOLICITOR]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[APPONESOLICITOR]").build()
            ))
            .build();

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", APPLICANT_1_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("2", APPLICANT_1_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", APPLICANT_2_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("1", APPLICANT_2_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", APPLICANT_1_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("1", APPLICANT_1_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", APPLICANT_2_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("2", APPLICANT_2_SOLICITOR));

        switchToSoleService.switchUserRoles(caseData, caseId);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", APPLICANT_1_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", APPLICANT_2_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", APPLICANT_1_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", APPLICANT_2_SOLICITOR)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
    }

    @Test
    void shouldSwitchCitizenAndSolicitorUserRolesIfApplicant2SolicitorTriggeredSwitchToSoleAndApplicant1Citizen() {
        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().solicitorRepresented(YES).build())
            .build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPTWOSOLICITOR]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", CREATOR))
            .thenReturn(getCaseAssignmentRequest("2", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", APPLICANT_2_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("1", APPLICANT_2_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", APPLICANT_1_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("1", APPLICANT_1_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("2", UserRole.APPLICANT_2));

        switchToSoleService.switchUserRoles(caseData, caseId);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", CREATOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", APPLICANT_2_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", APPLICANT_1_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", UserRole.APPLICANT_2)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
    }

    @Test
    void shouldSwitchSolicitorAndCitizenUserRolesIfApplicant2SolicitorTriggeredSwitchToSoleAndApplicant1Citizen() {
        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().solicitorRepresented(NO).build())
            .build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[APPONESOLICITOR]").build()
            ))
            .build();

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", APPLICANT_1_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("2", APPLICANT_1_SOLICITOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("1", UserRole.APPLICANT_2));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", CREATOR))
            .thenReturn(getCaseAssignmentRequest("1", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", APPLICANT_2_SOLICITOR))
            .thenReturn(getCaseAssignmentRequest("2", APPLICANT_2_SOLICITOR));

        switchToSoleService.switchUserRoles(caseData, caseId);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", APPLICANT_1_SOLICITOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", UserRole.APPLICANT_2)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", CREATOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", APPLICANT_2_SOLICITOR)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentRequest(String userId, UserRole role) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(getCaseAssignmentUserRole(role.getRole(), userId))
            ).build();
    }

    private CaseAssignmentUserRoleWithOrganisation getCaseAssignmentUserRole(String role, String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
            .organisationId(null)
            .caseDataId(String.valueOf(TEST_CASE_ID))
            .caseRole(role)
            .userId(userId)
            .build();
    }
}
