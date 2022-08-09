package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class SendAosPackToRespondentTest {

    @Mock
    private AosPackPrinter aosPackPrinter;

    @InjectMocks
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Test
    void shouldSendAosLetterToRespondentWhenSoleAndCourtService() {
        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        sendAosPackToRespondent.apply(caseDetails);

        verify(aosPackPrinter).sendAosLetterToRespondent(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendAosLetterToApplicant2WhenJointAndCourtService() {
        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        sendAosPackToRespondent.apply(caseDetails);

        verify(aosPackPrinter).sendAosLetterToRespondent(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotSendAosLetterToRespondentWhenNotACourtService() {

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        sendAosPackToRespondent.apply(caseDetails);

        verifyNoInteractions(aosPackPrinter);
    }
}
