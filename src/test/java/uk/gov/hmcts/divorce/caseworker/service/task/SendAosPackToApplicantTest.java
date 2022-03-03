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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendAosPackToApplicantTest {

    @Mock
    private AosPackPrinter aosPackPrinter;

    @InjectMocks
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Test
    void shouldNotSendAosPackToApplicantIfApplicantIsDigital() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        sendAosPackToApplicant.apply(caseDetails);

        verifyNoInteractions(aosPackPrinter);
    }

    @Test
    void shouldSendAosPackToApplicantIfApplicantIsOffline() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        doNothing().when(aosPackPrinter).sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        sendAosPackToApplicant.apply(caseDetails);

        verify(aosPackPrinter).sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        verifyNoMoreInteractions(aosPackPrinter);
    }
}
