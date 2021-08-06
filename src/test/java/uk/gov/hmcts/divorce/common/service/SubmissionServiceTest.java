package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SendSubmissionNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.divorce.common.service.task.SetState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SetState setState;

    @Mock
    private SetDateSubmitted setDateSubmitted;

    @Mock
    private SendSubmissionNotifications sendSubmissionNotifications;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void shouldProcessSubmissionCaseTasks() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setState.apply(caseDetails)).thenReturn(caseDetails);
        when(setDateSubmitted.apply(caseDetails)).thenReturn(caseDetails);
        when(sendSubmissionNotifications.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = submissionService.submitApplication(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setState).apply(caseDetails);
        verify(setDateSubmitted).apply(caseDetails);
        verify(sendSubmissionNotifications).apply(caseDetails);
    }
}
