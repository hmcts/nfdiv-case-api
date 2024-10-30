package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponseNotification;
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponsePartnerNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationResponse.CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationResponse.NO_REQUEST_FOR_INFORMATION_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildOfflineDraft;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;

@ExtendWith(MockitoExtension.class)
class CaseworkerRequestForInformationResponseTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CitizenRequestForInformationResponseNotification citizenRequestForInformationResponseNotification;

    @Mock
    private CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @InjectMocks
    private CaseworkerRequestForInformationResponse caseworkerRequestForInformationResponse;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRequestForInformationResponse.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE);
    }

    @Test
    void shouldReturnErrorWhenNoRequestForInformationOnCase() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationResponse.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_REQUEST_FOR_INFORMATION_ERROR);
    }

    @Test
    void shouldSetStateToRequestedInformationSubmittedWhenAllDocsProvided() {
        CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        buildOfflineDraft(caseDetails.getData(), RequestForInformationOfflineResponseSoleParties.APPLICANT, true, false, true);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationResponse.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationOfflineResponseDraft())
            .isEqualTo(new RequestForInformationOfflineResponseDraft());
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse requestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getLatestResponse();
        final Applicant applicant =
            caseDetails.getData().getApplicant1();
        assertThat(requestForInformationResponse.getRequestForInformationResponseParties())
            .isEqualTo(RequestForInformationResponseParties.APPLICANT1);
        assertThat(requestForInformationResponse.getRequestForInformationResponseName()).isEqualTo(applicant.getFullName());
        assertThat(requestForInformationResponse.getRequestForInformationResponseEmailAddress()).isEqualTo(applicant.getEmail());
        assertThat(requestForInformationResponse.getRequestForInformationResponseOffline()).isEqualTo(YES);
        assertThat(requestForInformationResponse.getRequestForInformationResponseDocs()).isNull();
        assertThat(requestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(requestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isNull();
        assertThat(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded()).isEqualTo(YES);
        assertThat(requestForInformationResponse.getRfiOfflineResponseDocs()).isNull();
    }

    @Test
    void shouldSetStateToAwaitingRequestedInformationWhenAllDocsNotProvided() {
        CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        buildOfflineDraft(caseDetails.getData(), RequestForInformationOfflineResponseSoleParties.APPLICANT, true, false, false);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationResponse.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingRequestedInformation);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationOfflineResponseDraft())
            .isEqualTo(new RequestForInformationOfflineResponseDraft());
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse requestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getLatestResponse();
        final Applicant applicant =
            caseDetails.getData().getApplicant1();
        assertThat(requestForInformationResponse.getRequestForInformationResponseParties())
            .isEqualTo(RequestForInformationResponseParties.APPLICANT1);
        assertThat(requestForInformationResponse.getRequestForInformationResponseName()).isEqualTo(applicant.getFullName());
        assertThat(requestForInformationResponse.getRequestForInformationResponseEmailAddress()).isEqualTo(applicant.getEmail());
        assertThat(requestForInformationResponse.getRequestForInformationResponseOffline()).isEqualTo(YES);
        assertThat(requestForInformationResponse.getRequestForInformationResponseDocs()).isNull();
        assertThat(requestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(requestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isNull();
        assertThat(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded()).isEqualTo(NO);
        assertThat(requestForInformationResponse.getRfiOfflineResponseDocs()).isNull();
    }

    @Test
    void shouldNotSendNotificationsOnSoleCaseIfRfiSentToOther() {
        CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.OTHER, false, false);
        addOfflineResponseToLatestRequestForInformation(caseDetails.getData(), RequestForInformationOfflineResponseSoleParties.OTHER);

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendNotificationsOnJointCaseIfRfiSentToOther() {
        CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.OTHER, false, false);
        addOfflineResponseToLatestRequestForInformation(caseDetails.getData(), RequestForInformationOfflineResponseJointParties.OTHER);

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotificationToRespondingPartyOnlyOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, false, false);
        caseDetails.setId(TEST_CASE_ID);

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotificationToRespondingPartyOnlyOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        caseDetails.setId(TEST_CASE_ID);

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToRespondingPartyFailsOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, false, false);
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponseNotification(
                citizenRequestForInformationResponseNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToRespondingPartyFailsOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponseNotification(
                citizenRequestForInformationResponseNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotificationToRespondingPartyAndPartnerOnJointCaseWhenRFISentToBoth() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        addOfflineResponseToLatestRequestForInformation(caseDetails.getData(), RequestForInformationOfflineResponseJointParties.APPLICANT1);
        caseDetails.setId(TEST_CASE_ID);

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
        verify(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToPartnerFails() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        addResponseToLatestRequestForInformation(caseDetails.getData(), caseDetails.getData().getApplicant1());
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponsePartnerNotification(
                citizenRequestForInformationResponsePartnerNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToBothPartiesFails() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        addResponseToLatestRequestForInformation(caseDetails.getData(), caseDetails.getData().getApplicant1());
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        caseworkerRequestForInformationResponse.submitted(caseDetails, caseDetails);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponseNotification(
                citizenRequestForInformationResponseNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponsePartnerNotification(
                citizenRequestForInformationResponsePartnerNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
    }
}
