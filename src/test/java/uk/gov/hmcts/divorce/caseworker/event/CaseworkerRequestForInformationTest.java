package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.RequestForInformationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.APPLICANT_1;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.APPLICANT_2;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.CASEWORKER_REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.NOT_ONLINE_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.NO_VALID_EMAIL_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.NO_VALID_EMAIL_PROVIDED_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.SOLICITOR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.THE_APPLICANT;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.USE_CORRECT_PARTY_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformation.USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class CaseworkerRequestForInformationTest {

    @Mock
    private RequestForInformationNotification requestForInformationNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerRequestForInformation caseworkerRequestForInformation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRequestForInformation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REQUEST_FOR_INFORMATION);
    }

    @Test
    void shouldReturnErrorWhenApplicantOfflineOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + THE_APPLICANT,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicantSolicitorOfflineOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + THE_APPLICANT + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1OfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1SolicitorOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant2OfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant2SolicitorOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1OnlineApplicant2OfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1OnlineApplicant2SolicitorOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1OfflineApplicant2OnlineOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1SolicitorOfflineApplicant2OnlineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant(MALE));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenBothApplicantsOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1,
            NOT_ONLINE_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenBothApplicantSolicitorsOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1 + SOLICITOR,
            NOT_ONLINE_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1OfflineApplicant2SolicitorOfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1,
            NOT_ONLINE_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1SolicitorOfflineApplicant2OfflineOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NOT_ONLINE_ERROR + APPLICANT_1 + SOLICITOR,
            NOT_ONLINE_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + THE_APPLICANT,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicantSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + THE_APPLICANT + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOfflineApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOnlineApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOfflineApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOnlineApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicantOfflineSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicantOnlineSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOfflineRespondentEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR + ".");
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOnlineRespondentEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR + ".");
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedRespondentOfflineSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR + SOLICITOR + ".");
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedRespondentOnlineSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR + SOLICITOR + ".");
    }

    @Test
    void shouldReturnErrorWhenNoOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldReturnErrorWhenEmptyOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress("");
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldReturnErrorWhenShortOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress("a@b.c");
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldValidateApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateBothApplicantEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1,
            NO_VALID_EMAIL_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant1EmailAndNoApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenApplicant2EmailAndNoApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateApplicant1SolicitorAndApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant2().setGender(MALE);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantSolicitorEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR,
            NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1SolicitorEmailAndApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2SolicitorEmailAndApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1EmailAndNoApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1,
            NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2EmailAndNoApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(3);
        assertThat(response.getErrors()).contains(
            NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR,
            NO_VALID_EMAIL_ERROR + APPLICANT_2,
            USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
        );
    }

    @Test
    void shouldValidateOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOnlineApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOfflineApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOnlineApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOfflineApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicant1OnlineSolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicant1OfflineSolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOnlineApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(getApplicant(MALE));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesUnrepresentedOfflineApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOnlineApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedOfflineApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_USER_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicant2OnlineSolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setAgreeToReceiveEmailsCheckbox(Set.of(Solicitor.Prayer.CONFIRM));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CORRECT_PARTY_ERROR);
    }

    @Test
    void shouldReturnErrorWhenOtherEmailMatchesRepresentedApplicant2OfflineSolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(
            TEST_SOLICITOR_EMAIL.toUpperCase()
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
    }

    @Test
    void shouldReturnErrorWhenNoOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldReturnErrorWhenEmptyOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress("");
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldReturnErrorWhenShortOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress("a@b.c");
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    @Test
    void shouldFailAboutToSubmitEventWhenNotificationFails() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        doThrow(new NotificationTemplateException(""))
            .when(notificationDispatcher).sendRequestForInformationNotification(requestForInformationNotification, caseData, TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors())
            .isEqualTo(Collections.singletonList(REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR + TEST_CASE_ID));
    }

    @Test
    void shouldClearDefaultRequestForInformationObjectDuringAboutToSubmitEvent() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().setRequestForInformation(new RequestForInformation());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDateTime(LocalDateTime.now());
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestForInformation();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant1WhenNotRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldAddAdditionalRequestToTopOfRequestListWhenListIsNotEmpty() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        final ListValue<RequestForInformation> request = new ListValue<>();
        request.setValue(new RequestForInformation());
        final List<ListValue<RequestForInformation>> requests = new ArrayList<>();
        requests.add(request);
        caseData.getRequestForInformationList().setRequestsForInformation(requests);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(2);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant1SolicitorWhenRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant1WhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant1SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant2WhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToApplicant2SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartiesToApplicantsWhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartiesToApplicantSolicitorsWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartiesToApplicant1SolicitorAndApplicant2WhenApplicant1RepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartiesToApplicant1AndApplicant2SolicitorWhenApplicant2RepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToOtherOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(OTHER);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(TEST_OTHER_EMAIL);
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(TEST_OTHER_NAME);
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

    @Test
    void shouldSetPartyToOtherOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation()
            .setRequestForInformationJointParties(RequestForInformationJointParties.OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getRequestsForInformation()).hasSize(1);

        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(RequestForInformationJointParties.OTHER);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(TEST_OTHER_EMAIL);
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(TEST_OTHER_NAME);
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformation.getRequestForInformationDateTime()).isNotNull();

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(InformationRequested);
    }

}
