package uk.gov.hmcts.divorce.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CitizenSubmissionServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private ApplicationSubmittedNotification notification;

    @Mock
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @InjectMocks
    private CitizenSubmissionService submissionService;


    @Test
    public void givenCaseSetSubmissionAndDueDateAndSendNotification() {
        final long caseId = 2L;
        CaseData caseData = caseData();

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDateTime = LocalDateTime.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        CaseData result = submissionService.submit(caseData, caseId);

        assertThat(result.getApplication().getDateSubmitted()).isEqualTo(expectedDateTime);
        assertThat(result.getDueDate()).isEqualTo(expectedDateTime.plusDays(14).toLocalDate());

        verify(notification).send(result, caseId);
        verifyNoInteractions(outstandingActionNotification);
    }

    @Test
    public void givenEventStartedWithCaseWithNoDocsThenChangeStateAwaitingHwfDecision() {
        final long caseId = 2L;
        CaseData caseData = caseData();
        caseData.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(MARRIAGE_CERTIFICATE));

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDateTime = LocalDateTime.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        CaseData result = submissionService.submit(caseData, caseId);

        assertThat(result.getApplication().getDateSubmitted()).isEqualTo(expectedDateTime);
        assertThat(result.getDueDate()).isEqualTo(expectedDateTime.plusDays(14).toLocalDate());

        verify(notification).send(result, caseId);
        verify(outstandingActionNotification).send(result, caseId);
    }

}
