package uk.gov.hmcts.divorce.bulkscan.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation;

import java.util.List;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;

@ExtendWith(MockitoExtension.class)
class ApplicationValidationTest {

    @Test
    void shouldReturnValidationErrorsWhenMandatoryFieldsForIssueAreNotPopulated() {
        final var caseData = invalidCaseData();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final List<String> result = ApplicationValidation.validateIssue(caseData);

        assertThat(result)
            .containsExactlyInAnyOrder(
                "ApplicationType cannot be empty or null",
                "Applicant2FirstName cannot be empty or null",
                "Applicant2LastName cannot be empty or null",
                "Applicant1FinancialOrder cannot be empty or null",
                "Applicant2Gender cannot be empty or null",
                "MarriageApplicant1Name cannot be empty or null",
                "Applicant1ContactDetailsType cannot be empty or null",
                "Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)",
                "MarriageDate cannot be empty or null",
                "JurisdictionConnections cannot be empty or null",
                "MarriageApplicant2Name cannot be empty or null",
                "PlaceOfMarriage cannot be empty or null",
                "Applicant1Gender cannot be empty or null"
            );
    }

    @Test
    void shouldReturnValidationErrorsWhenPersonalServiceWithConfidentialRespondent() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        final List<String> result = ApplicationValidation.validateIssue(caseData);

        Assertions.assertThat(result).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldReturnValidationErrorsWhenSolicitorServiceWithConfidentialRespondent() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        final List<String> result = ApplicationValidation.validateIssue(caseData);

        Assertions.assertThat(result).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldReturnValidationErrorIfCourtServiceWithOverseasRespondent() {
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
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        updatedCaseDetails.setData(caseData);

        final List<String> result = ApplicationValidation.validateIssue(caseData);

        Assertions.assertThat(result).contains("You may not select court service if "
            + "respondent has an international address.");
    }

    @Test
    void shouldNotThrowErrorIfCourtServiceOverseasRespondentNotConfidentialButJointApp() {
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
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        updatedCaseDetails.setData(caseData);

        final List<String> result = ApplicationValidation.validateIssue(caseData);

        Assertions.assertThat(result).isEmpty();
    }
}
