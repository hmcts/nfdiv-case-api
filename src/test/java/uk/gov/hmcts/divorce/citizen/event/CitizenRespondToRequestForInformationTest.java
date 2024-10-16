package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenRespondToRequestForInformation.CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.CitizenRespondToRequestForInformation.CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.CitizenRespondToRequestForInformation.CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.CitizenRespondToRequestForInformation.CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenRespondToRequestForInformation.UNABLE_TO_DETERMINE_CITIZEN_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addDocumentToRequestForInformationResponseDraft;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;

@ExtendWith(MockitoExtension.class)
class CitizenRespondToRequestForInformationTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CitizenRequestForInformationResponseNotification citizenRequestForInformationResponseNotification;

    @Mock
    private CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @InjectMocks
    private CitizenRespondToRequestForInformation citizenRespondToRequestForInformation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenRespondToRequestForInformation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION);
    }

    @Test
    void shouldReturnErrorIfUnableToDetermineCitizen() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(false);
        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, beforeDetails);

        assertThat(response.getErrors()).containsExactly(UNABLE_TO_DETERMINE_CITIZEN_ERROR + TEST_CASE_ID);
    }

    @Test
    void shouldReturnErrorIfRfiSolePartyIsOther() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.OTHER, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
                + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
                + RequestForInformationSoleParties.OTHER
                + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
                + TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorIfRfiJointPartyIsOther() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.OTHER, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
                + "1 "
                + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
                + RequestForInformationJointParties.OTHER
                + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
                + TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorIfRespondentAndSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
                + "2 "
                + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
                + APPLICANT
                + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
                + TEST_CASE_ID
        );
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicantOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(NO);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);
        Applicant applicant = caseData.getApplicant1();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(NO);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithCannotUploadResponseFromApplicantOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(YES);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);
        Applicant applicant = caseData.getApplicant1();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).isNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(YES);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingRequestedInformation);
    }

    @Test
    void shouldReturnErrorIfApplicant1AndRfiJointPartyIsApplicant2() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT2, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
                + "1 "
                + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
                + RequestForInformationJointParties.APPLICANT2
                + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
                + TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorIfApplicant2AndRfiJointPartyIsApplicant1() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant2(draft);

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
                + "2 "
                + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
                + RequestForInformationJointParties.APPLICANT1
                + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
                + TEST_CASE_ID
        );
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant1OnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(NO);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);
        Applicant applicant = caseData.getApplicant1();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(NO);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant2OnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT2, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(NO);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant2(draft);
        Applicant applicant = caseData.getApplicant2();

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant2())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(NO);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant1OnJointCaseWhenRfiForBoth() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(NO);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);
        Applicant applicant = caseData.getApplicant1();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(NO);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant2OnJointCaseWhenRfiForBoth() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.BOTH, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(NO);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant2(draft);
        Applicant applicant = caseData.getApplicant2();

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant2())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(NO);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithCannotUploadResponseFromApplicant1OnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(YES);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1(draft);
        Applicant applicant = caseData.getApplicant1();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(YES);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingRequestedInformation);
    }

    @Test
    void shouldPopulateDataWithCannotUploadResponseFromApplicant2OnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT2, false, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        draft.setRfiDraftResponseCannotUploadDocs(YES);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant2(draft);
        Applicant applicant = caseData.getApplicant2();

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenRespondToRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);
        assertThat(response.getData().getRequestForInformationList().getRequestForInformationResponseApplicant2())
            .isEqualTo(new RequestForInformationResponseDraft());

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(applicant.getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(applicant.getFullName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseCannotUploadDocs()).isEqualTo(YES);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingRequestedInformation);
    }

    @Test
    void shouldSendNotificationToRespondingPartyOnlyOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, false, false);
        caseDetails.setId(TEST_CASE_ID);

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotificationToRespondingPartyAndPartnerOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        caseDetails.setId(TEST_CASE_ID);

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

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
    void shouldReturnErrorWhenSendNotificationToRespondingPartyFailsOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, false, false);
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponseNotification(
            citizenRequestForInformationResponseNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

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

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponseNotification(
                citizenRequestForInformationResponseNotification,
                caseDetails.getData(),
                TEST_CASE_ID
            );
        });
        verify(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );
    }

    @Test
    void shouldReturnErrorWhenSendNotificationToPartnerFails() {
        final CaseDetails<CaseData, State> caseDetails =
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationTemplateException.class).when(notificationDispatcher).sendRequestForInformationResponsePartnerNotification(
            citizenRequestForInformationResponsePartnerNotification,
            caseDetails.getData(),
            TEST_CASE_ID
        );

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

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
            getRequestForInformationCaseDetails(RequestForInformationJointParties.APPLICANT1, false, false);
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

        citizenRespondToRequestForInformation.submitted(caseDetails, caseDetails);

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
