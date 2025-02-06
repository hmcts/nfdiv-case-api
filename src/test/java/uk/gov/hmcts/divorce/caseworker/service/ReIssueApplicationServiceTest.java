package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.ResetAosFields;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetReIssueAndDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.JudicialSeparationReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ReIssueApplicationServiceTest {

    @Mock
    private SetPostIssueState setPostIssueState;

    @Mock
    private GenerateApplication generateApplication;

    @Mock
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Mock
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Mock
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Mock
    private SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    @Mock
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Mock
    private SetReIssueAndDueDate setReIssueAndDueDate;

    @Mock
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Mock
    private GenerateD10Form generateD10Form;

    @Mock
    private GenerateD84Form generateD84Form;

    @Mock
    private ResetAosFields resetAosFields;

    @InjectMocks
    private ReIssueApplicationService reIssueApplicationService;

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsDigitalAos() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplicant2().setOffline(NO);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);
        expectedCaseData.getApplicant2().setOffline(NO);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsOfflineAos() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setOffline(YES);
        expectedCaseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenJudicialSeparationApplicationWhenReissueTypeIsOfflineAos() {

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setJudicialSeparationReissueOption(JudicialSeparationReissueOption.OFFLINE_AOS);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);


        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setOffline(YES);
        expectedCaseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);
        expectedCaseData.getApplication().setJudicialSeparationReissueOption(null);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsReissueCase() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(REISSUE_CASE);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setPreviousReissueOption(REISSUE_CASE);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenJudicialSeparationSApplicationWhenReissueTypeIsReissueCase() {

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setJudicialSeparationReissueOption(JudicialSeparationReissueOption.REISSUE_CASE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setPreviousReissueOption(REISSUE_CASE);
        expectedCaseData.getApplication().setJudicialSeparationReissueOption(null);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForSolicitorApplicationWhenReissueTypeIsDigitalAos() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplicant2().setOffline(NO);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);
        expectedCaseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);
        expectedCaseData.getApplicant2().setOffline(NO);

        assertThat(response.getData().getApplicant2()).isEqualTo(expectedCaseData.getApplicant2());
        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForSolicitorApplicationWhenReissueTypeIsOfflineAos() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setOffline(YES);
        expectedCaseData.getApplicant2().setEmail(null);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);
        expectedCaseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldRunReIssueApplicationTasksForSolicitorApplicationWhenReissueTypeIsReissueCase() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setReissueOption(REISSUE_CASE);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setNoticeOfProceedingDetailsForRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD84Form.apply(caseDetails)).thenReturn(caseDetails);
        when(resetAosFields.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);
        expectedCaseData.getApplication().setPreviousReissueOption(REISSUE_CASE);

        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());

        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
        verifyNoInteractions(sendApplicationIssueNotifications);
    }

    @Test
    void shouldThrowInvalidReissueOptionExceptionWhenReissueOptionIsNotSet() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setReissueOption(null);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        assertThatThrownBy(() -> reIssueApplicationService.process(caseDetails))
            .isExactlyInstanceOf(InvalidReissueOptionException.class)
            .hasMessage("Invalid reissue option for CaseId: 1616591401473378");

    }

    @Test
    public void shouldNotSendBulkPrintNotificationsWhenReissueOptionIsDigitalAos() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData());
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        reIssueApplicationService.sendNotifications(caseDetails, DIGITAL_AOS);

        verify(sendApplicationIssueNotifications).apply(caseDetails);
        verifyNoInteractions(sendAosPackToApplicant);
        verifyNoInteractions(sendAosPackToRespondent);
    }

    @Test
    public void shouldSendEmailAndBulkPrintNotificationsWhenReissueOptionIsOfflineAos() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData());
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);

        reIssueApplicationService.sendNotifications(caseDetails, OFFLINE_AOS);

        verify(sendApplicationIssueNotifications).apply(caseDetails);
        verify(sendAosPackToApplicant).apply(caseDetails);
        verify(sendAosPackToRespondent).apply(caseDetails);
    }

    @Test
    public void shouldSendEmailAndBulkPrintNotificationsWhenReissueOptionIsReissueCaseAndNotPersonalService() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);

        reIssueApplicationService.sendNotifications(caseDetails, REISSUE_CASE);

        verify(sendApplicationIssueNotifications).apply(caseDetails);
        verify(sendAosPackToApplicant).apply(caseDetails);
        verify(sendAosPackToRespondent).apply(caseDetails);
    }

    @Test
    public void shouldNotSendEmailAndBulkPrintNotificationsWhenReissueOptionIsReissueCaseAndPersonalService() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);

        reIssueApplicationService.sendNotifications(caseDetails, REISSUE_CASE);

        verifyNoInteractions(sendApplicationIssueNotifications);
        verify(sendAosPackToApplicant).apply(caseDetails);
        verify(sendAosPackToRespondent).apply(caseDetails);
    }

    @Test
    void shouldThrowReissueProcessingExceptionWhenSendingNotificationsWithUnknownReissueOption() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        assertThatThrownBy(() -> reIssueApplicationService.sendNotifications(caseDetails, null))
            .isExactlyInstanceOf(InvalidReissueOptionException.class)
            .hasMessage("Exception occurred while sending reissue application notifications for case id 1616591401473378");
    }
}
