package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.Organisation;
import uk.gov.hmcts.divorce.noticeofchange.model.OrganisationPolicy;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerNoticeOfChange.CASEWORKER_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOL_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerNoticeOfChangeTest {

    @Mock
    private NoticeOfChangeService noticeOfChangeService;

    @Mock
    private SolicitorValidationService solicitorValidationService;

    @InjectMocks
    private CaseworkerNoticeOfChange noticeOfChange;

    @Test
    public void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        noticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_NOTICE_OF_CHANGE);
    }

    @Test
    public void shouldReturnValidationErrorWhenUserDoesNotExist() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyDigital(YES)
            .areTheyRepresented(YES)
            .build());

        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());

        when(solicitorValidationService.validateEmailBelongsToOrgUser(TEST_SOLICITOR_EMAIL, details.getId(), TEST_ORG_ID))
            .thenReturn(List.of("Error"));

        var result = noticeOfChange.midEvent(details, details);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()).contains("Error");
    }

    @Test
    public void shouldNotReturnValidationErrorWhenUserDoesExist() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyDigital(YES)
            .areTheyRepresented(YES)
            .build());

        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());

        when(solicitorValidationService.validateEmailBelongsToOrgUser(TEST_SOLICITOR_EMAIL, details.getId(), TEST_ORG_ID))
            .thenReturn(Collections.emptyList());

        var result = noticeOfChange.midEvent(details, details);

        assertThat(result.getErrors()).isNullOrEmpty();
    }

    @Test
    public void shouldNotValidateWhenOfflineCitizen() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyDigital(NO)
            .areTheyRepresented(NO)
            .build());


        var result = noticeOfChange.midEvent(details, details);

        assertThat(result.getErrors()).isNullOrEmpty();

        verifyNoInteractions(solicitorValidationService);
    }

    @Test
    public void shouldNotValidateWhenOfflineSolicitor() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyDigital(NO)
            .areTheyRepresented(YES)
            .build());


        var result = noticeOfChange.midEvent(details, details);

        assertThat(result.getErrors()).isNullOrEmpty();

        verifyNoInteractions(solicitorValidationService);
    }

    @Test
    public void shouldAddErrorIfEmailIsMissingAndIsDigitalNoc() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyDigital(YES)
            .areTheyRepresented(YES)
            .build());
        details.getData().getApplicant1().getSolicitor().setEmail(null);
        details.getData().getApplicant1().getSolicitor().setOrganisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().organisationId(TEST_ORG_ID).build())
            .build());


        var result = noticeOfChange.midEvent(details, details);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0))
            .isEqualTo("No email provided - please provide an email for the solicitor you wish to add");
    }

    @Test
    public void shouldProcessOfflineNoticeOfChangeWithoutInvokingNocService() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
                .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
                .areTheyDigital(NO)
                .areTheyRepresented(NO)
            .build());

        List<String> roles = List.of(UserRole.CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant1().getSolicitor().getOrganisationPolicy())
            .isEqualTo(
                OrganisationPolicy.<UserRole>builder()
                    .orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR)
                    .build());
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(NO);

        verify(noticeOfChangeService, never()).revokeCaseAccess(details.getId(), beforeDetails.getData().getApplicant1(), roles);
    }

    @Test
    public void shouldMoveCaseOfflineAndRevokeDigitalSolicitorAccess() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        beforeDetails.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(NO)
            .build());

        List<String> roles = List.of(UserRole.CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant1().getSolicitor().getOrganisationPolicy())
            .isEqualTo(
                OrganisationPolicy.<UserRole>builder()
                    .orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR)
                    .build());
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(NO);

        verify(noticeOfChangeService).revokeCaseAccess(details.getId(), beforeDetails.getData().getApplicant1(), roles);
    }

    @Test
    public void shouldReplaceAccessWithinOrganisation() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        beforeDetails.getData().getApplicant1().getSolicitor().setEmail(TEST_SOL_USER_EMAIL);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        beforeDetails.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        List<String> roles = List.of(UserRole.CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        Solicitor newSolicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationId(TEST_ORG_ID)
                    .build())
                .orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR)
                .build())
            .build();

        verify(noticeOfChangeService).changeAccessWithinOrganisation(newSolicitor, roles, APPLICANT_1_SOLICITOR.getRole(), details.getId());
    }

    @Test
    public void shouldBeTreatedAsOfflineNocWhenEmailAndOrgAreSameAsBeforeEvent() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        beforeDetails.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        verifyNoInteractions(noticeOfChangeService);
    }

    @Test
    public void shouldApplyNoticeOfChangeDecision() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        beforeDetails.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId("SECOND_ORG_ID")
            .build());
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        List<String> roles = List.of(UserRole.CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        verify(noticeOfChangeService).applyNocDecisionAndGrantAccessToNewSol(
            details.getId(),
            details.getData().getApplicant1(),
            beforeDetails.getData().getApplicant1(),
            roles,
            APPLICANT_1_SOLICITOR.getRole());
    }

    @Test
    public void shouldHandleNullBeforeOrgPolicyGracefully() {
        var details = getCaseDetails();
        var beforeDetails = getCaseDetails();
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(Organisation.builder()
            .organisationId(TEST_ORG_ID)
            .build());
        beforeDetails.getData().getApplicant1().getSolicitor().setOrganisationPolicy(null);
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        List<String> roles = List.of(UserRole.CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        verify(noticeOfChangeService).applyNocDecisionAndGrantAccessToNewSol(
            details.getId(),
            details.getData().getApplicant1(),
            beforeDetails.getData().getApplicant1(),
            roles,
            APPLICANT_1_SOLICITOR.getRole());
    }

    @Test
    public void shouldHandleNullRepresentationGracefully() {
        var details = getCaseDetails();
        details.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(NoticeOfChange.WhichApplicant.APPLICANT_1)
            .areTheyRepresented(null)
            .build());

        var result = noticeOfChange.aboutToSubmit(details, getCaseDetails());

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(NO);
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant1(applicantRepresentedBySolicitor());
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplicant1().setOffline(NO);
        data.getApplicant2().setOffline(NO);
        data.getApplicant1().getSolicitor().setOrganisationPolicy(OrganisationPolicy.<UserRole>builder()
                .orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR)
            .build());
        data.getApplicant2().getSolicitor().setOrganisationPolicy(OrganisationPolicy.<UserRole>builder()
                .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .build());
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }

}
