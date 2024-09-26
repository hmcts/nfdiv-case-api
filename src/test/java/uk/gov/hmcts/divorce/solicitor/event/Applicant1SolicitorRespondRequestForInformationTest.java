package uk.gov.hmcts.divorce.solicitor.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorRespondRequestForInformation.APP_1_SOLICITOR_RESPOND_REQUEST_INFO;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorRespondRequestForInformation.MUST_ADD_DOCS_OR_DETAILS_ERROR;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorRespondRequestForInformation.UNABLE_TO_SUBMIT_RESPONSE_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addDocumentToRequestForInformationResponseDraft;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationResponseDraft;

@ExtendWith(MockitoExtension.class)
class Applicant1SolicitorRespondRequestForInformationTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private Applicant1SolicitorRespondRequestForInformation applicant1SolicitorRespondRequestForInformation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1SolicitorRespondRequestForInformation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APP_1_SOLICITOR_RESPOND_REQUEST_INFO);
    }

    @Test
    void shouldReturnErrorIfNoDetailsOrDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(MUST_ADD_DOCS_OR_DETAILS_ERROR);
    }

    @Test
    void shouldNotReturnErrorIfDocumentsButNoDetails() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();
        final CaseData caseData = caseDetails.getData();
        addDocumentToRequestForInformationResponseDraft(getRequestForInformationResponseDraft(caseData, caseData.getApplicant1()));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorIfDetailsButNoDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponseApplicant1Solicitor().setRfiDraftResponseDetails(TEST_TEXT);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldClearDefaultResponseObjectAfterAddingResponseToRequestObjectOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        final RequestForInformationResponseDraft draftResponse =
            getRequestForInformationResponseDraft(response.getData(), response.getData().getApplicant1());
        assertThat(draftResponse.getRfiDraftResponseDetails()).isNull();
        assertThat(draftResponse.getRfiDraftResponseDocs()).isNull();
    }

    @Test
    void shouldClearDefaultResponseObjectAfterAddingResponseToRequestObjectOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT1, true, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        final RequestForInformationResponseDraft draftResponse =
            getRequestForInformationResponseDraft(response.getData(), response.getData().getApplicant1());
        assertThat(draftResponse.getRfiDraftResponseDetails()).isNull();
        assertThat(draftResponse.getRfiDraftResponseDocs()).isNull();
    }

    @Test
    void shouldReturnErrorIfSolicitorNotAssignedToApplicant1() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(UNABLE_TO_SUBMIT_RESPONSE_ERROR + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicantSolicitorOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails();
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);
        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
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
    void shouldPopulateDataWithResponseFromApplicant1SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT1, true, false);
        final CaseData caseData = caseDetails.getData();
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);
        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
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
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());
        RequestForInformationResponseDraft draft = new RequestForInformationResponseDraft();
        draft.setRfiDraftResponseDetails(TEST_TEXT);
        addDocumentToRequestForInformationResponseDraft(draft);
        caseData.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(draft);
        caseDetails.setState(RequestedInformationSubmitted);
        final Solicitor app1Solicitor = caseData.getApplicant1().getSolicitor();
        final Solicitor app2Solicitor = caseData.getApplicant2().getSolicitor();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(2);

        final RequestForInformationResponse responseRequestForInformationResponseApp1 =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseEmailAddress())
            .isEqualTo(app1Solicitor.getEmail());
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseName())
            .isEqualTo(app1Solicitor.getName());
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponseApp1.getRequestForInformationResponseDocs()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponseApp2 =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(1).getValue();

        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseParties()).isEqualTo(APPLICANT2SOLICITOR);
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseEmailAddress())
            .isEqualTo(app2Solicitor.getEmail());
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseName())
            .isEqualTo(app2Solicitor.getName());
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDocs()).hasSize(1);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }
}
