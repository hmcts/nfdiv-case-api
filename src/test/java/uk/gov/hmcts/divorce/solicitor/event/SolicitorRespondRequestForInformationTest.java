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
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;
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
            .contains(SOLICITOR_CREATE);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicantSolicitorOnSoleCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(SOLE_APPLICATION, APPLICANT, true, false);
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    @Test
    void shouldPopulateDataWithResponseFromApplicant1SolicitorOnJointCase() {
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(JOINT_APPLICATION, APPLICANT1, true, false);
        final CaseData caseData = caseDetails.getData();
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDetails(TEST_TEXT);
        addDocumentToResponse(caseData);

        when(ccdAccessService.isApplicant1(any(), any())).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorRespondRequestForInformation.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses()).hasSize(1);

        final RequestForInformationResponse responseRequestForInformationResponse =
            response.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationResponses().get(0).getValue();

        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseParties()).isEqualTo(APPLICANT1SOLICITOR);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDetails()).isEqualTo(TEST_TEXT);
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDateTime()).isNotNull();
        assertThat(responseRequestForInformationResponse.getRequestForInformationResponseDocs()).hasSize(1);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(RequestedInformationSubmitted);
    }

    private CaseDetails<CaseData, State> getCaseDetails(ApplicationType applicationType,
                                       RequestForInformationSoleParties requestForInformationSoleParties,
                                       Boolean applicantRepresented,
                                       Boolean applicant2Represented
    ) {
        CaseData caseData = getBaseData(applicationType, applicantRepresented, applicant2Represented);
        setBaseRequestParties(caseData, requestForInformationSoleParties);
        setBaseRequestValues(caseData);

        return getBaseDetails(caseData);
    }

    private CaseDetails<CaseData, State> getCaseDetails(ApplicationType applicationType,
                                       RequestForInformationJointParties requestForInformationJointParties,
                                       Boolean applicantRepresented,
                                       Boolean applicant2Represented
    ) {
        CaseData caseData = getBaseData(applicationType, applicantRepresented, applicant2Represented);
        setBaseRequestParties(caseData, requestForInformationJointParties);
        setBaseRequestValues(caseData);

        return getBaseDetails(caseData);
    }

    private void setBaseRequestParties(CaseData caseData, RequestForInformationSoleParties requestForInformationSoleParties) {
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(
            requestForInformationSoleParties
        );
    }

    private void setBaseRequestParties(CaseData caseData, RequestForInformationJointParties requestForInformationJointParties) {
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            requestForInformationJointParties
        );
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
        final ListValue<DivorceDocument> uploadedDocument = documentWithType(null);
        List<ListValue<DivorceDocument>> docs = new ArrayList<>();
        docs.add(uploadedDocument);
        caseData.getRequestForInformationList().getRequestForInformationResponse().setRequestForInformationResponseDocs(docs);
    }
}
