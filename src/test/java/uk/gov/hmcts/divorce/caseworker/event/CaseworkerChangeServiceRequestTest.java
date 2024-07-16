package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerChangeServiceRequest.CASEWORKER_CHANGE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;

@ExtendWith(MockitoExtension.class)
class CaseworkerChangeServiceRequestTest {

    @InjectMocks
    private CaseworkerChangeServiceRequest caseworkerChangeServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerChangeServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CHANGE_SERVICE_REQUEST);
    }

    @Test
    void shouldThrowErrorIfPersonalServiceConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldThrowErrorIfSolicitorServiceConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldChangeServiceRequestPersonalServiceNonConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldChangeServiceRequestSolicitorServiceNonConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldChangeServiceRequestCourtServiceConfidentialButIsOverseas() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);
        applicant2.setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldThrowErrorServiceRequestCourtServiceNotConfidentialButIsOverseas() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);
        applicant2.setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select court service if respondent"
            + " has an international address.");
    }

    @Test
    void shouldNotThrowErrorServiceRequestCourtServiceNotConfidentialButIsOverseasAndJointApp() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);
        applicant2.setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }
}
