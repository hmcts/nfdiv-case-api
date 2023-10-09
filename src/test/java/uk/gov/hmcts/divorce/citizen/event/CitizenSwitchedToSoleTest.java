package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSole.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CitizenSwitchedToSoleTest {

    @Mock
    private Applicant1SwitchToSoleNotification applicant1SwitchToSoleNotification;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Applicant2SwitchToSoleNotification applicant2SwitchToSoleNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CitizenSwitchedToSole citizenSwitchedToSole;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenSwitchedToSole.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE);
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant1SwitchToSoleShouldSetApplicationTypeToSoleAndSendNotifications() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant1SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant1SwitchToSoleWithApplicant2NotLinkedShouldSendNotifications() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        setValidCaseInviteData(caseDataBefore);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        verify(notificationDispatcher).send(applicant1SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        verify(ccdAccessService, times(0)).unlinkApplicant2FromCase(anyLong(), anyString());
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getCaseInvite().accessCode()).isNull();
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant1SwitchToSoleWithApplicant2LinkedShouldRemoveAccessCodeAndSendNotifications() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        setValidCaseInviteData(caseDataBefore);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        caseDataBefore.setApplicant2(
            Applicant.builder()
                .email(TEST_APPLICANT_2_EMAIL)
                .build()
        );

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        verify(notificationDispatcher).send(applicant1SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getCaseInvite().accessCode()).isNull();
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant2SwitchToSoleShouldSetApplicationTypeToSoleAndSendNotifications() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseDataBefore);
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        caseDataBefore.setApplicant2(
            Applicant.builder()
                .email(TEST_APPLICANT_2_EMAIL)
                .build()
        );
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app2-token");
        when(ccdAccessService.isApplicant1("app2-token", caseId)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        verify(notificationDispatcher).send(applicant2SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        verifyNoInteractions(applicant1SwitchToSoleNotification);
        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveApplicant2AddressIfPrivate() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseDataBefore);
        Applicant applicant2 =
            Applicant.builder()
                .address(
                    AddressGlobalUK.builder()
                        .addressLine1("123 The Street")
                        .postTown("The town")
                        .county("County Durham")
                        .country("England")
                        .postCode("POSTCODE")
                        .build())
                .contactDetailsType(PRIVATE)
                .email(TEST_APPLICANT_2_EMAIL)
                .build();
        caseData.setApplicant2(applicant2);
        caseDataBefore.setApplicant2(applicant2);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        assertThat(response.getData().getApplicant2().getAddress()).isNull();
        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldNotRemoveApplicant2AddressIfNotPrivate() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseDataBefore);
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        Applicant applicant2 = Applicant.builder()
            .address(
                AddressGlobalUK.builder()
                    .addressLine1("123 The Street")
                    .postTown("The town")
                    .county("County Durham")
                    .country("England")
                    .postCode("POSTCODE")
                    .build())
            .contactDetailsType(PUBLIC)
            .email(TEST_APPLICANT_2_EMAIL)
            .build();
        caseData.setApplicant2(applicant2);
        caseDataBefore.setApplicant2(applicant2);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        assertThat(response.getData().getApplicant2().getAddress())
            .isEqualTo(
                AddressGlobalUK.builder()
                    .addressLine1("123 The Street")
                    .postTown("The town")
                    .county("County Durham")
                    .country("England")
                    .postCode("POSTCODE")
                    .build()
            );
        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveApplicant2Answers() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));

        setValidCaseInviteData(caseData);
        caseData.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_EMAIL, ACCESS_CODE, null));
        caseData.getDocuments().setApplicant2DocumentsUploaded(new ArrayList<>());
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2HelpWithFees(HelpWithFees.builder().build());
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().setApplicant2AgreeToReceiveEmails(YES);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(new HashSet<>());
        caseData.getApplication().setApplicant2CannotUpload(NO);
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct");
        caseData.getApplication().setApplicant2ReminderSent(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        CaseData caseDataBefore = validJointApplicant1CaseData();
        setValidCaseInviteData(caseDataBefore);
        final CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        Applicant applicant2 = Applicant.builder()
            .firstName("Bob")
            .middleName("The")
            .lastName("Build")
            .email("bob@buildings.com")
            .gender(MALE)
            .financialOrder(YES)
            .lastNameChangedWhenMarried(YES)
            .address(
                AddressGlobalUK.builder()
                    .addressLine1("123 The Street")
                    .postTown("The town")
                    .county("County Durham")
                    .country("England")
                    .postCode("POSTCODE")
                    .build())
            .contactDetailsType(PRIVATE)
            .email(TEST_APPLICANT_2_EMAIL)
            .build();
        caseData.setApplicant2(applicant2);
        caseDataBefore.setApplicant2(applicant2);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        assertThat(response.getData().getApplicant2())
            .isEqualTo(
                Applicant.builder()
                    .firstName("Bob")
                    .middleName("The")
                    .lastName("Build")
                    .gender(MALE)
                    .build());

        assertThat(response.getData().getCaseInvite())
            .isEqualTo(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_EMAIL)
                .build());

        assertThat(response.getData().getDocuments().getApplicant2DocumentsUploaded()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ScreenHasMarriageBroken()).isNull();
        assertThat(response.getData().getApplication().getApplicant2HelpWithFees()).isNull();
        assertThat(response.getData().getApplication().getApplicant2StatementOfTruth()).isNull();
        assertThat(response.getData().getApplication().getApplicant2AgreeToReceiveEmails()).isNull();
        assertThat(response.getData().getApplication().getApplicant2CannotUploadSupportingDocument()).isNull();
        assertThat(response.getData().getApplication().getApplicant2CannotUpload()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ConfirmApplicant1Information()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ReminderSent()).isNull();

        assertThat(response.getData().getApplicant2().getApplicantPrayer().getPrayerDissolveDivorce()).isNull();
        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
    }

    @Test
    void shouldUnlinkApp2OnAboutToSubmitCallback() {
        long caseId = 12345L;
        CaseData caseData = CaseData.builder().caseInvite(
            CaseInvite.builder().applicant2UserId("app2-user-id").build()
        ).build();
        CaseData caseDataBefore = CaseData.builder().caseInvite(
            CaseInvite.builder().applicant2UserId("app2-user-id").build()
        ).build();
        CaseDetails<CaseData, State> beforeDetails =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();
        caseDataBefore.setApplicant2(
            Applicant.builder()
                .email(TEST_APPLICANT_2_EMAIL)
                .build()
        );
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();

        citizenSwitchedToSole.aboutToSubmit(caseDetails, beforeDetails);

        verify(ccdAccessService).unlinkApplicant2FromCase(eq(caseId), eq("app2-user-id"));
    }

    @Test
    void shouldNotUnlinkApp2OnAboutToSubmitCallback() {
        long caseId = 12345L;
        CaseData caseData = CaseData.builder().caseInvite(
            CaseInvite.builder().accessCode("access-code").build()
        ).build();
        CaseData caseDataBefore = CaseData.builder().caseInvite(
            CaseInvite.builder().accessCode("access-code").build()
        ).build();
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        CaseDetails<CaseData, State> caseDetailsBefore =
            CaseDetails.<CaseData, State>builder().data(caseDataBefore).id(caseId).build();

        citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetailsBefore);

        verify(ccdAccessService, times(0)).unlinkApplicant2FromCase(anyLong(), anyString());
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveJurisdictionAnswers() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validApplicant2CaseData();
        setValidCaseInviteData(caseData);
        caseData.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_EMAIL, ACCESS_CODE, null));

        caseData.getApplication().getJurisdiction().setApplicant1Domicile(YesOrNo.YES);
        caseData.getApplication().getJurisdiction().setApplicant2Domicile(YesOrNo.YES);
        caseData.getApplication().getJurisdiction().setResidualEligible(NO);
        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT));
        caseData.getApplication().getJurisdiction().setApplicant1Residence(NO);
        caseData.getApplication().getJurisdiction().setApplicant2Residence(NO);
        caseData.getApplication().getJurisdiction().setBothLastHabituallyResident(NO);
        caseData.getApplication().getJurisdiction().setApp1HabituallyResLastTwelveMonths(NO);
        caseData.getApplication().getJurisdiction().setApp1HabituallyResLastSixMonths(NO);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(caseId).build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplication().getJurisdiction()).isNull();
    }

    private CaseData setValidCaseInviteData(CaseData caseData) {
        caseData.setCaseInvite(
            CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_EMAIL)
                .applicant2UserId("app2-user-id")
                .build()
        );
        return caseData;
    }
}
