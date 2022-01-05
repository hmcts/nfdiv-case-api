package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSole.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
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
    private IdamService idamService;

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
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email(TEST_USER_EMAIL)
            .id("app1")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant1SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant1SwitchToSoleWithApplicant2NotLinkedShouldRemoveAccessCodeAndSendNotifications() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        caseData.getCaseInvite().setAccessCode(ACCESS_CODE);
        caseData.getCaseInvite().setApplicant2UserId(null);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn(TEST_USER_EMAIL);

        final var userDetails = UserDetails.builder()
            .email(TEST_USER_EMAIL)
            .id("app1")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant1SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getCaseInvite().getAccessCode()).isNull();
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant2SwitchToSoleShouldSetApplicationTypeToSoleAndSendNotifications() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email(TEST_APPLICANT_2_EMAIL)
            .id("app2")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2SwitchToSoleNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        verifyNoInteractions(applicant1SwitchToSoleNotification);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveApplicant2AddressIfPrivate() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        caseData.setApplicant2(
            Applicant.builder()
                .homeAddress(
                    AddressGlobalUK.builder()
                        .addressLine1("123 The Street")
                        .postTown("The town")
                        .county("County Durham")
                        .country("England")
                        .postCode("POSTCODE")
                        .build())
                .contactDetailsType(PRIVATE)
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email(TEST_USER_EMAIL)
            .id("app1")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().getHomeAddress()).isNull();
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldNotRemoveApplicant2AddressIfNotPrivate() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        caseData.setApplicant2(
            Applicant.builder()
                .homeAddress(
                    AddressGlobalUK.builder()
                        .addressLine1("123 The Street")
                        .postTown("The town")
                        .county("County Durham")
                        .country("England")
                        .postCode("POSTCODE")
                        .build())
                .contactDetailsType(PUBLIC)
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email(TEST_USER_EMAIL)
            .id("app1")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().getHomeAddress())
            .isEqualTo(
                AddressGlobalUK.builder()
                    .addressLine1("123 The Street")
                    .postTown("The town")
                    .county("County Durham")
                    .country("England")
                    .postCode("POSTCODE")
                    .build()
            );
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveApplicant2Answers() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        setValidCaseInviteData(caseData);
        caseData.getCaseInvite().setAccessCode(ACCESS_CODE);
        caseData.setApplicant2(
            Applicant.builder()
                .firstName("Bob")
                .middleName("The")
                .lastName("Build")
                .email("bob@buildings.com")
                .gender(MALE)
                .financialOrder(YES)
                .lastNameChangedWhenMarried(YES)
                .homeAddress(
                    AddressGlobalUK.builder()
                        .addressLine1("123 The Street")
                        .postTown("The town")
                        .county("County Durham")
                        .country("England")
                        .postCode("POSTCODE")
                        .build())
                .contactDetailsType(PRIVATE)
                .build()
        );
        caseData.setApplicant2DocumentsUploaded(new ArrayList<>());
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2HelpWithFees(HelpWithFees.builder().build());
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().setApplicant2PrayerHasBeenGiven(YES);
        caseData.getApplication().setApplicant2AgreeToReceiveEmails(YES);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(new HashSet<>());
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct");
        caseData.getApplication().setApplicant2ReminderSent(YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email(TEST_USER_EMAIL)
            .id("app1")
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

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

        assertThat(response.getData().getApplicant2DocumentsUploaded()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ScreenHasMarriageBroken()).isNull();
        assertThat(response.getData().getApplication().getApplicant2HelpWithFees()).isNull();
        assertThat(response.getData().getApplication().getApplicant2StatementOfTruth()).isNull();
        assertThat(response.getData().getApplication().getApplicant2PrayerHasBeenGiven()).isNull();
        assertThat(response.getData().getApplication().getApplicant2AgreeToReceiveEmails()).isNull();
        assertThat(response.getData().getApplication().getApplicant2CannotUploadSupportingDocument()).isNull();
        assertThat(response.getData().getApplication().getApplicant2CannotUploadSupportingDocument()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ConfirmApplicant1Information()).isNull();
        assertThat(response.getData().getApplication().getApplicant2ReminderSent()).isNull();
    }

    private CaseData setValidCaseInviteData(CaseData caseData) {
        caseData.setCaseInvite(
            CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_EMAIL)
                .applicant2UserId("app2")
                .build()
        );
        return caseData;
    }
}
