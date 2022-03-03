package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SendAosPackToRespondentTest {

    @Mock
    private AosPackPrinter aosPackPrinter;

    @InjectMocks
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Test
    void shouldNotPrintAosIfApplicationIsPersonalServiceMethodWhenAboutToSubmit() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = sendAosPackToRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verifyNoInteractions(aosPackPrinter);
    }

    @Test
    void shouldNotPrintAosIfApplicationIsCourtServiceAndApplicant2IsOverseasWhenAboutToSubmit() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        Applicant applicant2 = respondent();
        applicant2.setAddress(
            AddressGlobalUK
                .builder()
                .country("France")
                .postTown("Paris")
                .addressLine1("Line 1")
                .build()
        );
        caseData.setApplicant2(applicant2);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = sendAosPackToRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verifyNoInteractions(aosPackPrinter);
    }

    @Test
    void shouldPrintAosAndSetDueDateIfNotPersonalServiceAndRespondentIsNotRepresentedAndIsNotOverseas() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        Applicant applicant2 = respondent();
        applicant2.setAddress(
            AddressGlobalUK
                .builder()
                .country("UK")
                .postTown("London")
                .addressLine1("Line 1")
                .build()
        );
        caseData.setApplicant2(applicant2);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = sendAosPackToRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verify(aosPackPrinter).sendAosLetterToRespondent(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldPrintAosAndUpdateCaseDataIfNotPersonalServiceAndRespondentIsRepresented() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicant2(respondentWithDigitalSolicitor());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = sendAosPackToRespondent.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(TEST_SOLICITOR_EMAIL, TEST_ORG_NAME);
        verify(aosPackPrinter).sendAosLetterToRespondent(caseData, TEST_CASE_ID);
    }
}
