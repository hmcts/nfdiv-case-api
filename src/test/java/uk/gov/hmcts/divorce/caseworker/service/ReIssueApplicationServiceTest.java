package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetReIssueAndDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.ReissueProcessingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ReIssueApplicationServiceTest {

    @Mock
    private SetPostIssueState setPostIssueState;

    @Mock
    private GenerateDivorceApplication generateDivorceApplication;

    @Mock
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Mock
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Mock
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Mock
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Mock
    private SetReIssueAndDueDate setReIssueAndDueDate;

    @Mock
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Mock
    private GenerateD10Form generateD10Form;

    @InjectMocks
    private ReIssueApplicationService reIssueApplicationService;

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsDigitalAos() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsOfflineAos() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateDivorceApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setOffline(YES);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldRunReIssueApplicationTasksForCitizenApplicationWhenReissueTypeIsReissueCase() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.getApplication().setReissueOption(REISSUE_CASE);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateDivorceApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldRunReIssueApplicationTasksForSolicitorApplicationWhenReissueTypeIsDigitalAos() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
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
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateDivorceApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setOffline(YES);
        expectedCaseData.getApplicant2().setEmail(null);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
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
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(setReIssueAndDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateDivorceApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = reIssueApplicationService.process(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldThrowReissueProcessingExceptionWhenReissueOptionIsNotSet() {

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
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        assertThatThrownBy(() -> reIssueApplicationService.process(caseDetails))
            .isExactlyInstanceOf(ReissueProcessingException.class)
            .hasMessage("Exception occurred while processing reissue application for case id 1");

    }
}
