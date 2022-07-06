package uk.gov.hmcts.divorce.legaladvisor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.AwaitingClarificationPrinter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class LegalAdvisorRejectedDecisionNotificationTest {

    @Mock
    private AwaitingClarificationPrinter awaitingClarificationPrinter;

    @InjectMocks
    private LegalAdvisorRejectedDecisionNotification notification;

    @Test
    void shouldSendConditionalOrderRejectedLettersToApplicant1IfOffline() {
        CaseData caseData = validApplicant1CaseData();

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(awaitingClarificationPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1());
    }

    @Test
    void shouldSendConditionalOrderRejectedLettersToApplicant2IfOfflineAndJointApplication() {
        CaseData caseData = validJointApplicant1CaseData();

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(awaitingClarificationPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2());
    }

    @Test
    void shouldNotSendConditionalOrderRejectedLettersToApplicant2IfOfflineAndSoleApplication() {
        CaseData caseData = validApplicant1CaseData();

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(awaitingClarificationPrinter);
    }
}
