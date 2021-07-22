package uk.gov.hmcts.divorce.caseworker.service.updater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;
import static uk.gov.hmcts.divorce.testutil.UpdaterTestUtil.caseDataContext;

@ExtendWith(MockitoExtension.class)
class SendAosPackTest {

    private static final long DUE_DATE_OFFSET_DAYS = 30L;

    @Mock
    private AosPackPrinter aosPackPrinter;

    @Mock
    private Clock clock;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private SendAosPack sendAosPack;

    @BeforeEach
    void setPageSize() {
        setField(sendAosPack, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldNotPrintAosIfApplicationIsPersonalServiceMethodWhenAboutToSubmit() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(PERSONAL_SERVICE);
        final var caseDataContext = caseDataContext(caseData);

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseDataContext result = sendAosPack.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData().getDueDate()).isNull();
        assertThat(result.getCaseData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verifyNoInteractions(aosPackPrinter, clock);
    }

    @Test
    void shouldPrintAosAndSetDueDateIfNotPersonalServiceAndRespondentIsNotRepresented() {

        setMockClock(clock);
        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);
        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());
        final var caseDataContext = caseDataContext(caseData);

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseDataContext result = sendAosPack.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData().getDueDate()).isEqualTo(expectedDueDate);
        assertThat(result.getCaseData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verify(aosPackPrinter).print(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldPrintAosAndUpdateCaseDataIfNotPersonalServiceAndRespondentIsRepresented() {

        setMockClock(clock);
        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);
        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondentWithDigitalSolicitor());
        final var caseDataContext = caseDataContext(caseData);

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseDataContext result = sendAosPack.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData().getDueDate()).isEqualTo(expectedDueDate);
        assertThat(result.getCaseData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(YES, TEST_SOLICITOR_EMAIL, TEST_ORG_NAME);
        verify(aosPackPrinter).print(caseData, TEST_CASE_ID);
    }
}