package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosResponseLetterPackToApplicant;
import uk.gov.hmcts.divorce.common.service.task.GenerateAndLinkRespondentAnswers;
import uk.gov.hmcts.divorce.common.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.divorce.common.service.task.SetSubmitAosState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitAosServiceTest {

    @Mock
    private SetSubmitAosState setSubmitAosState;

    @Mock
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Mock
    private GenerateAndLinkRespondentAnswers generateAndLinkRespondentAnswers;

    @Mock
    private SendAosNotifications sendAosNotifications;

    @Mock
    private SendAosResponseLetterPackToApplicant sendAosResponseLetterPackToApplicant;

    @InjectMocks
    private SubmitAosService submitAosService;

    @Test
    void shouldProcessSolicitorSubmitAos() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setSubmitAosState.apply(caseDetails)).thenReturn(caseDetails);
        when(setSubmissionAndDueDate.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(generateAndLinkRespondentAnswers.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = submitAosService.submitAos(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setSubmitAosState).apply(caseDetails);
        verify(setSubmissionAndDueDate).apply(caseDetails);
        verify(generateAndLinkRespondentAnswers).apply(caseDetails);
    }

    @Test
    void shouldProcessSolicitorSubmitAosNotifications() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(sendAosNotifications.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(sendAosResponseLetterPackToApplicant.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = submitAosService.submitAosNotifications(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(sendAosNotifications).apply(caseDetails);
        verify(sendAosResponseLetterPackToApplicant).apply(caseDetails);
    }

    @Test
    void shouldProcessSubmitOfflineAos() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setSubmitAosState.apply(caseDetails)).thenReturn(caseDetails);
        when(setSubmissionAndDueDate.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = submitAosService.submitOfflineAos(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setSubmitAosState).apply(caseDetails);
        verify(setSubmissionAndDueDate).apply(caseDetails);
    }
}
