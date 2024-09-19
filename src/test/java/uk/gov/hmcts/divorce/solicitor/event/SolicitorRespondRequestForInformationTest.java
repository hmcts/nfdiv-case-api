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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorRespondRequestForInformation.MUST_ADD_DOCS_OR_DESCRIPTION_ERROR;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorRespondRequestForInformation.SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorRespondRequestForInformation.UNABLE_TO_SUBMIT_RESPONSE_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class SolicitorRespondRequestForInformationTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorRespondRequestForInformation solicitorRespondRequestForInformation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorRespondRequestForInformation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION);
    }

    @Test
    void shouldReturnErrorIfNoDescriptionOrDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(MUST_ADD_DOCS_OR_DESCRIPTION_ERROR);
    }

    @Test
    void shouldNotReturnErrorIfDocumentsButNoDescription() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();
        final CaseData caseData = caseDetails.getData();
        addDocumentToResponse(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorIfDescriptionButNoDocuments() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldClearDefaultResponseObjectAfterAddingResponseToRequestObjectOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        final RequestForInformationResponse defaultRequestForInformationResponse =
            response.getData().getRequestForInformationList().getRequestForInformationResponse();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseParties()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseName()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseEmailAddress()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDetails()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDocs()).isNull();
    }

    @Test
    void shouldClearDefaultResponseObjectAfterAddingResponseToRequestObjectOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(APPLICANT1, true, false);
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        final RequestForInformationResponse defaultRequestForInformationResponse =
            response.getData().getRequestForInformationList().getRequestForInformationResponse();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseParties()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseName()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseEmailAddress()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDetails()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNull();
        assertThat(defaultRequestForInformationResponse.getRequestForInformationResponseDocs()).isNull();
    }

    @Test
    void shouldReturnErrorIfSolicitorNotAssignedToEitherApplicant() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(false);
        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(UNABLE_TO_SUBMIT_RESPONSE_ERROR + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicantSolicitorOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails();
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);
        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
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
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(APPLICANT1, true, false);
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);
        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
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
    void shouldPopulateDataWithResponseFromApplicant2SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(APPLICANT2, false, true);
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);
        addDocumentToResponse(caseData);
        final Solicitor solicitor = caseData.getApplicant2().getSolicitor();

        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
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
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(2);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponsesFromApplicant1SolicitorAndApplicant2SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(BOTH, true, true);
        final CaseData caseData = caseDetails.getData();
        addResponseToLatestRequest(caseData, caseData.getApplicant1());
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        caseDetails.setState(RequestedInformationSubmitted);
        final Solicitor app1Solicitor = caseData.getApplicant1().getSolicitor();
        final Solicitor app2Solicitor = caseData.getApplicant2().getSolicitor();


        when(ccdAccessService.isApplicant2(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
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
        assertThat(responseRequestForInformationResponseApp2.getRequestForInformationResponseDocs()).isNull();

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

    private CaseDetails<CaseData, State> getCaseDetails() {
        CaseData caseData = getBaseData(SOLE_APPLICATION, true, false);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        setBaseRequestValues(caseData);

        return getBaseDetails(caseData);
    }

    private CaseDetails<CaseData, State> getCaseDetails(RequestForInformationJointParties requestForInformationJointParties,
                                       Boolean applicantRepresented,
                                       Boolean applicant2Represented
    ) {
        CaseData caseData = getBaseData(JOINT_APPLICATION, applicantRepresented, applicant2Represented);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            requestForInformationJointParties
        );
        setBaseRequestValues(caseData);

        return getBaseDetails(caseData);
    }

    private CaseData getBaseData(ApplicationType applicationType, Boolean applicantRepresented, Boolean applicant2Represented) {
        final CaseData caseData = caseData();
        caseData.setApplicationType(applicationType);
        if (applicantRepresented) {
            caseData.setApplicant1(applicantRepresentedBySolicitor());
        }
        if (applicant2Represented) {
            caseData.setApplicant2(applicantRepresentedBySolicitor());
        } else {
            caseData.setApplicant2(getApplicant(MALE));
        }

        return caseData;
    }

    private void setBaseRequestValues(CaseData caseData) {
        caseData.getRequestForInformationList().getRequestForInformation().setValues(caseData);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
    }

    private CaseDetails<CaseData, State> getBaseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingDocuments);
        details.setId(TEST_CASE_ID);

        return details;
    }

    private void addDocumentToResponse(CaseData caseData) {
        final List<ListValue<DivorceDocument>> docs =
            caseData.getRequestForInformationList().getRequestForInformationResponse().getRequestForInformationResponseDocs();
        final ListValue<DivorceDocument> uploadedDocument = documentWithType(null);

        if (isEmpty(docs)) {
            List<ListValue<DivorceDocument>> newDocs = new ArrayList<>();
            newDocs.add(uploadedDocument);
            caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDocs(newDocs);
        } else {
            docs.add(0, uploadedDocument);
        }
    }

    private void addResponseToLatestRequest(CaseData caseData, Applicant applicant) {
        RequestForInformationResponse requestForInformationResponse =
            caseData.getRequestForInformationList().getRequestForInformationResponse();
        requestForInformationResponse.setValues(
            applicant,
            caseData.getApplicant1().equals(applicant) ? APPLICANT1SOLICITOR : APPLICANT2SOLICITOR
        );
        requestForInformationResponse.setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getLatestRequest();
        requestForInformation.addResponseToList(requestForInformationResponse);

        caseData.getRequestForInformationList().setRequestForInformationResponse(new RequestForInformationResponse());
    }
}
