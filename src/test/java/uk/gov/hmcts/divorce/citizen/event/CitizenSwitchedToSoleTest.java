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
import uk.gov.hmcts.divorce.citizen.notification.SwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSole.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CitizenSwitchedToSoleTest {

    @Mock
    private SwitchToSoleNotification switchToSoleNotification;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

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
        caseDetails.setState(State.AwaitingApplicant2Response);
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleNotification).sendApplicant1SwitchToSoleNotificationToApplicant1(caseData, caseDetails.getId());
        verify(switchToSoleNotification).sendApplicant1SwitchToSoleNotificationToApplicant2(caseData, caseDetails.getId());
        verifyNoMoreInteractions(switchToSoleNotification);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseForApplicant2SwitchToSoleShouldSetApplicationTypeToSoleAndSendNotifications() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(State.Applicant2Approved);
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleNotification).sendApplicant2SwitchToSoleNotificationToApplicant1(caseData, caseDetails.getId());
        verify(switchToSoleNotification).sendApplicant2SwitchToSoleNotificationToApplicant2(caseData, caseDetails.getId());
        verifyNoMoreInteractions(switchToSoleNotification);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenApplicant2ScreenHasMarriageBrokenIsNoThenOnlyApplicant1NotificationIsSent() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);
        caseData.setApplicationType(JOINT_APPLICATION);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleNotification).sendApplicant1SwitchToSoleNotificationToApplicant1(caseData, caseDetails.getId());
        verifyNoMoreInteractions(switchToSoleNotification);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldRemoveApplicant2AddressIfPrivate() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
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
                .keepContactDetailsConfidential(YES)
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSole.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().getHomeAddress()).isNull();
    }

    @Test
    void givenEventStartedWithValidJointCaseShouldNotRemoveApplicant2AddressIfNotPrivate() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
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
                .keepContactDetailsConfidential(NO)
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

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
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setCaseInvite(
            CaseInvite.builder()
                .accessCode("QA34TR89")
                .applicant2InviteEmailAddress("bob@buildings.com")
                .applicant2UserId("1234")
                .build()
        );
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
                .keepContactDetailsConfidential(YES)
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
                .applicant2InviteEmailAddress("bob@buildings.com")
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
}
