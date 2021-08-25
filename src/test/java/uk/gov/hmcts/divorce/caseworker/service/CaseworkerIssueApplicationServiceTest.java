package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCitizenRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateMiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPack;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.MiniApplicationRemover;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationServiceTest {

    @Mock
    private MiniApplicationRemover miniApplicationRemover;

    @Mock
    private GenerateMiniApplication generateMiniApplication;

    @Mock
    private GenerateRespondentSolicitorAosInvitation generateRespondentSolicitorAosInvitation;

    @Mock
    private GenerateCitizenRespondentAosInvitation generateCitizenRespondentAosInvitation;

    @Mock
    private SendAosPack sendAosPack;

    @Mock
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Mock
    private SendAosNotifications sendAosNotifications;

    @Mock
    private SetDueDate setDueDate;

    @Mock
    private Clock clock;

    @InjectMocks
    private IssueApplicationService issueApplicationService;

    @Test
    void shouldRunIssueApplicationTasksForCitizenApplication() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        setMockClock(clock);

        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);
        when(setDueDate.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = issueApplicationService.issueApplication(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setIssueDate(getExpectedLocalDate());

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldRunIssueApplicationTasksForSolicitorApplication() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        setMockClock(clock);

        when(generateRespondentSolicitorAosInvitation.apply(caseDetails)).thenReturn(caseDetails);
        when(generateCitizenRespondentAosInvitation.apply(caseDetails)).thenReturn(caseDetails);
        when(miniApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(generateMiniApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPack.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosNotifications.apply(caseDetails)).thenReturn(caseDetails);
        when(setDueDate.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = issueApplicationService.issueApplication(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setIssueDate(getExpectedLocalDate());
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }
}
