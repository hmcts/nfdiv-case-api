package uk.gov.hmcts.divorce.solicitor.event;

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
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponsePartnerNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorRespondRequestForInformation.NOT_AUTHORISED_TO_RESPOND_ERROR;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorRespondRequestForInformation.APP_2_SOLICITOR_RESPOND_REQUEST_INFO;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorRespondRequestForInformation.MUST_ADD_DOCS_OR_DETAILS_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildDraft;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationResponseDraft;

@ExtendWith(MockitoExtension.class)
class Applicant2SolicitorRespondRequestForInformationTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @InjectMocks
    private Applicant2SolicitorRespondRequestForInformation applicant2SolicitorRespondRequestForInformation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorRespondRequestForInformation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APP_2_SOLICITOR_RESPOND_REQUEST_INFO);
    }

    @Test
    void shouldReturnErrorIfRequestForOtherParty() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(OTHER, true, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NOT_AUTHORISED_TO_RESPOND_ERROR);
    }

    @Test
    void shouldReturnErrorIfRequestForApplicant1() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT1, true, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NOT_AUTHORISED_TO_RESPOND_ERROR);
    }

    @Test
    void shouldReturnErrorIfSoleApplication() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NOT_AUTHORISED_TO_RESPOND_ERROR);
    }

    @Test
    void shouldNotReturnErrorIfRequestForApplicant2() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void shouldNotReturnErrorIfRequestForBoth() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(BOTH, false, true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void shouldReturnErrorIfNoDetailsOrDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        buildDraft(caseData, caseData.getApplicant2(), false, false, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(MUST_ADD_DOCS_OR_DETAILS_ERROR);
    }

    @Test
    void shouldNotReturnErrorIfDocumentsButNoDetails() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        buildDraft(caseData, caseData.getApplicant2(), false, true, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorIfDetailsButNoDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        buildDraft(caseData, caseData.getApplicant2(), true, false, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldClearDefaultResponseObjectAfterAddingResponseToRequestObjectOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        buildDraft(caseData, caseData.getApplicant2(), true, true, false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        final RequestForInformationResponseDraft draftResponse =
            getRequestForInformationResponseDraft(response.getData(), response.getData().getApplicant2());
        assertThat(draftResponse.getRfiDraftResponseDetails()).isNull();
        assertThat(draftResponse.getRfiDraftResponseDocs()).isNull();
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant2SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        buildDraft(caseData, caseData.getApplicant2(), true, true, false);
        final Solicitor solicitor = caseData.getApplicant2().getSolicitor();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2SOLICITOR);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(solicitor.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(solicitor.getName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponsesFromApplicant1SolicitorAndApplicant2SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(BOTH, true, true);
        final CaseData caseData = caseDetails.getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());
        buildDraft(caseData, caseData.getApplicant2(), true, true, false);
        caseDetails.setState(RequestedInformationSubmitted);
        final Solicitor app1Solicitor = caseData.getApplicant1().getSolicitor();
        final Solicitor app2Solicitor = caseData.getApplicant2().getSolicitor();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(2);

        final RequestForInformationResponse responseRequestForInformationResponseApp2 =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2SOLICITOR);
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseEmailAddress())
            .isEqualTo(app2Solicitor.getEmail());
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseName())
            .isEqualTo(app2Solicitor.getName());
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDocs()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponseApp1 =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(1).getValue();

        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseEmailAddress())
            .isEqualTo(app1Solicitor.getEmail());
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseName())
            .isEqualTo(app1Solicitor.getName());
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDocs()).hasSize(1);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldSendNotificationToOtherPartyOnJointCaseWhenRFISentToBoth() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        addResponseToLatestRequestForInformation(caseDetails.getData(), caseDetails.getData().getApplicant2());
        caseDetails.setId(TEST_CASE_ID);

        applicant2SolicitorRespondRequestForInformation.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToOtherPartyFails() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        addResponseToLatestRequestForInformation(caseDetails.getData(), caseDetails.getData().getApplicant2());
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        applicant2SolicitorRespondRequestForInformation.submitted(caseDetails, caseDetails);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponsePartnerNotification(
                citizenRequestForInformationResponsePartnerNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
    }
}
