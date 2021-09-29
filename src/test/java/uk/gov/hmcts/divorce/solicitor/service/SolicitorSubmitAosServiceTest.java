package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.AddRespondentAnswersLink;
import uk.gov.hmcts.divorce.solicitor.service.task.GenerateRespondentAnswersDoc;
import uk.gov.hmcts.divorce.solicitor.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.divorce.solicitor.service.task.SetSubmitAosState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitAosServiceTest {

    @Mock
    private SetSubmitAosState setSubmitAosState;

    @Mock
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Mock
    private GenerateRespondentAnswersDoc respondentAnswersDoc;

    @Mock
    private AddRespondentAnswersLink addRespondentAnswersLink;

    @InjectMocks
    private SolicitorSubmitAosService solicitorSubmitAosService;

    @Test
    void shouldProcessSolicitorSubmitAos() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setSubmitAosState.apply(caseDetails)).thenReturn(caseDetails);
        when(setSubmissionAndDueDate.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(respondentAnswersDoc.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(addRespondentAnswersLink.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = solicitorSubmitAosService.submitAos(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setSubmitAosState).apply(caseDetails);
        verify(setSubmissionAndDueDate).apply(caseDetails);
        verify(respondentAnswersDoc).apply(caseDetails);
        verify(addRespondentAnswersLink).apply(caseDetails);
    }
}
