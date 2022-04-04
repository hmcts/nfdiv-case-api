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
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.divorce.caseworker.service.task.SetIssueDate;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationServiceTest {

    @Mock
    private SetPostIssueState setPostIssueState;

    @Mock
    private DivorceApplicationRemover divorceApplicationRemover;

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
    private SetDueDateAfterIssue setDueDateAfterIssue;

    @Mock
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Mock
    private GenerateD10Form generateD10Form;

    @Mock
    private SetServiceType setServiceType;

    @Mock
    private SetIssueDate setIssueDate;

    @InjectMocks
    private IssueApplicationService issueApplicationService;

    @Test
    void shouldRunIssueApplicationTasksForCitizenApplication() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setServiceType.apply(caseDetails)).thenReturn(caseDetails);
        when(setIssueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setPostIssueState.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(divorceApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(generateDivorceApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToRespondent.apply(caseDetails)).thenReturn(caseDetails);
        when(setDueDateAfterIssue.apply(caseDetails)).thenReturn(caseDetails);
        when(sendApplicationIssueNotifications.apply(caseDetails)).thenReturn(caseDetails);
        when(sendAosPackToApplicant.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = issueApplicationService.issueApplication(caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }
}
