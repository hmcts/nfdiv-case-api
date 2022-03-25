package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
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
    void shouldNotSendAosPackToApplicantIfApplicantIsRepresented() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor
                .builder()
                .name("test sol")
                .email("testsol@test.com")
                .build()
        );
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        sendAosPackToApplicant.apply(caseDetails);

        verifyNoInteractions(aosPackPrinter);
    }

    @Test
    void shouldSendAosPackToApplicantIfApplicantIsNotRepresented() {
        final var caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        doNothing().when(aosPackPrinter).sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        sendAosPackToApplicant.apply(caseDetails);

        verify(aosPackPrinter).sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        verifyNoMoreInteractions(aosPackPrinter);
    }

    @Test
    void shouldSendOverseasAosPackToApplicantIfApplicantAndRespondentAreNotRepresentedAndResppndentIsOverseas() {
        final var caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        sendAosPackToApplicant.apply(caseDetails);

        verify(aosPackPrinter).sendOverseasAosLetterToApplicant(caseData, TEST_CASE_ID);

        verifyNoMoreInteractions(aosPackPrinter);
    }
}
