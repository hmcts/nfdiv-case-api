package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SendSubmissionNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetApplicant2Email;
import uk.gov.hmcts.divorce.common.service.task.SetApplicantOfflineStatus;
import uk.gov.hmcts.divorce.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.divorce.common.service.task.SetDefaultOrganisationPolicies;
import uk.gov.hmcts.divorce.common.service.task.SetStateAfterSubmission;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SetStateAfterSubmission setStateAfterSubmission;

    @Mock
    private SetDateSubmitted setDateSubmitted;

    @Mock
    private SetApplicant2Email setApplicant2Email;

    @Mock
    private SetApplicantOfflineStatus setApplicantOfflineStatus;

    @Mock
    private SendSubmissionNotifications sendSubmissionNotifications;

    @Mock
    private SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void shouldProcessSubmissionCaseTasks() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setStateAfterSubmission.apply(caseDetails)).thenReturn(caseDetails);
        when(setDateSubmitted.apply(caseDetails)).thenReturn(caseDetails);
        when(setApplicant2Email.apply(caseDetails)).thenReturn(caseDetails);
        when(setApplicantOfflineStatus.apply(caseDetails)).thenReturn(caseDetails);
        when(setDefaultOrganisationPolicies.apply(caseDetails)).thenReturn(caseDetails);
        when(sendSubmissionNotifications.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final CaseDetails<CaseData, State> result = submissionService.submitApplication(caseDetails);

        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setStateAfterSubmission).apply(caseDetails);
        verify(setDateSubmitted).apply(caseDetails);
        verify(setApplicant2Email).apply(caseDetails);
        verify(setApplicantOfflineStatus).apply(caseDetails);
        verify(setDefaultOrganisationPolicies).apply(caseDetails);
        verify(sendSubmissionNotifications).apply(caseDetails);
    }
}
