package uk.gov.hmcts.divorce.common.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderGrantedDocumentPack;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class ResendConditionalOrderPronouncedNotificationTest {

    private static final DocumentPackInfo TEST_DOCUMENT_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(CONDITIONAL_ORDER_GRANTED, java.util.Optional.of(CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID)),
        ImmutableMap.of(CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID, CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME)
    );

    public static final String THE_LETTER_ID = "the-letter-id";
    @Mock
    private LetterPrinter printer;

    @Mock
    private ConditionalOrderGrantedDocumentPack conditionalOrderGrantedDocumentPack;
    @InjectMocks
    private ResendConditionalOrderPronouncedNotification underTest;

    @Test
    public void shouldSendLetterToOfflineApplicant1WhenCoPronouncedCoverLetterRegenerated() {
        final var caseId = TEST_CASE_ID;
        final var data = validJointApplicant1CaseData();
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(YES);

        when(conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1())).thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(conditionalOrderGrantedDocumentPack.getLetterId()).thenReturn(THE_LETTER_ID);

        underTest.sendToApplicant1Offline(data, caseId);

        verify(printer).sendLetters(
            data,
            caseId,
            data.getApplicant1(),
            TEST_DOCUMENT_PACK_INFO,
            THE_LETTER_ID
        );
    }

    @Test
    public void shouldNotSendLetterToApplicant1WhenCoPronouncedCoverLetterRegeneratedIsNO() {
        final var caseId = TEST_CASE_ID;
        final var data = validJointApplicant1CaseData();
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(NO);

        underTest.sendToApplicant1Offline(data, caseId);

        verifyNoInteractions(printer);
    }

    @Test
    public void shouldSendLetterToOfflineApplicant2WhenCoPronouncedCoverLetterRegenerated() {
        final var caseId = TEST_CASE_ID;
        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setOffline(NO);
        data.getApplicant2().setCoPronouncedCoverLetterRegenerated(YES);

        when(conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1())).thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(conditionalOrderGrantedDocumentPack.getLetterId()).thenReturn(THE_LETTER_ID);

        underTest.sendToApplicant2Offline(data, caseId);

        verify(printer).sendLetters(
            data,
            caseId,
            data.getApplicant2(),
            TEST_DOCUMENT_PACK_INFO,
            THE_LETTER_ID
        );
    }

    @Test
    public void shouldNotSendLetterToApplicant2WhenCoPronouncedCoverLetterRegeneratedIsNO() {
        final var caseId = TEST_CASE_ID;
        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setCoPronouncedCoverLetterRegenerated(NO);

        underTest.sendToApplicant2Offline(data, caseId);

        verifyNoInteractions(printer);
    }
}
