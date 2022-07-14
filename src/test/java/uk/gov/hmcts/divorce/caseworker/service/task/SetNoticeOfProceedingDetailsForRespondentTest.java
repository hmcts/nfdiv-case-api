package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SetNoticeOfProceedingDetailsForRespondentTest {

    @InjectMocks
    private SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    @Test
    void shouldSetNoticeOfProceedingDetailsToRespondentWhenSoleAndCourtServiceAndRespondentIsRepresented() {
        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondentWithDigitalSolicitor());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        CaseDetails<CaseData, State> result = setNoticeOfProceedingDetailsForRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains("solicitor@test.com", "Test Organisation");
    }

    @Test
    void shouldSetNoticeOfProceedingDetailsToApplicant2WhenJointAndCourtServiceAndApplicant2IsRepresented() {
        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondentWithDigitalSolicitor());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        CaseDetails<CaseData, State> result = setNoticeOfProceedingDetailsForRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains("solicitor@test.com", "Test Organisation");
    }

    @Test
    void shouldNotSetNoticeOfProceedingDetailsWhenNotACourtService() {

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.setApplicant2(respondentWithDigitalSolicitor());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        CaseDetails<CaseData, State> result = setNoticeOfProceedingDetailsForRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getNoticeOfProceedingsEmail()).isNull();
        assertThat(result.getData().getAcknowledgementOfService().getNoticeOfProceedingsSolicitorFirm()).isNull();
    }

    @Test
    void shouldNotSetNoticeOfProceedingSolicitorFirmWhenCourtServiceAndRespondentIsNotRepresented() {

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        Applicant respondent = respondent();
        respondent.setEmail("respondent@gm.com");
        caseData.setApplicant2(respondent);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        CaseDetails<CaseData, State> result = setNoticeOfProceedingDetailsForRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getNoticeOfProceedingsEmail()).isEqualTo("respondent@gm.com");
        assertThat(result.getData().getAcknowledgementOfService().getNoticeOfProceedingsSolicitorFirm()).isNull();
    }
}
