package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class ResendConditionalOrderPronouncedNotificationTest {

    @Mock
    private ConditionalOrderPronouncedPrinter conditionalOrderPronouncedPrinter;

    @InjectMocks
    private ResendConditionalOrderPronouncedNotification underTest;

    @Test
    public void shouldSendLetterToOfflineApplicant1WhenCoPronouncedCoverLetterRegenerated() {
        final var caseId = 1234567890123456L;
        final var data = validJointApplicant1CaseData();
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(YES);

        underTest.sendToApplicant1Offline(data, caseId);

        verify(conditionalOrderPronouncedPrinter).sendLetter(data, caseId, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);
    }

    @Test
    public void shouldNotSendLetterToApplicant1WhenCoPronouncedCoverLetterRegeneratedIsNO() {
        final var caseId = 1234567890123456L;
        final var data = validJointApplicant1CaseData();
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(NO);

        underTest.sendToApplicant1Offline(data, caseId);

        verifyNoInteractions(conditionalOrderPronouncedPrinter);
    }

    @Test
    public void shouldSendLetterToOfflineApplicant2WhenCoPronouncedCoverLetterRegenerated() {
        final var caseId = 1234567890123456L;
        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setOffline(NO);
        data.getApplicant2().setCoPronouncedCoverLetterRegenerated(YES);

        underTest.sendToApplicant2Offline(data, caseId);

        verify(conditionalOrderPronouncedPrinter).sendLetter(data, caseId, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);
    }

    @Test
    public void shouldNotSendLetterToApplicant2WhenCoPronouncedCoverLetterRegeneratedIsNO() {
        final var caseId = 1234567890123456L;
        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setCoPronouncedCoverLetterRegenerated(NO);

        underTest.sendToApplicant2Offline(data, caseId);

        verifyNoInteractions(conditionalOrderPronouncedPrinter);
    }
}
