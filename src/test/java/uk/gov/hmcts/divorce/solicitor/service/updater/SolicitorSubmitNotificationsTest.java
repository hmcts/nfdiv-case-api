package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitNotificationsTest {

    @Mock
    private CaseDataContext caseDataContext;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @Mock
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @InjectMocks
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Test
    void shouldSendApplicantAndSolicitorNotification() {

        final var caseData = CaseData.builder().build();

        when(caseDataContext.getCaseData()).thenReturn(caseData);
        when(caseDataContext.getCaseId()).thenReturn(TEST_CASE_ID);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final var result = solicitorSubmitNotification.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result, is(caseDataContext));

        verify(solicitorSubmittedNotification).send(caseData, TEST_CASE_ID);
    }
}
