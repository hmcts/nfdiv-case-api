package uk.gov.hmcts.divorce.common.notification;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.notification.ResendConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ResendConditionalOrderPronouncedNotificationTest {

    private static final Long TEST_CASE_ID = 123L;
    private static final String LETTER_ID = "letterId";
    private static final DocumentPackInfo APPLICANT_1_TEST_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_TEST_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );


    @Mock
    private ConditionalOrderPronouncedDocumentPack conditionalOrderGrantedDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private ResendConditionalOrderPronouncedNotification notification;

    @Test
    void shouldSendToApplicant1IfRegenerated() {
        CaseData data = caseData();
        when(conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1()))
            .thenReturn(APPLICANT_1_TEST_PACK_INFO);
        when(conditionalOrderGrantedDocumentPack.getLetterId()).thenReturn(LETTER_ID);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(YES);

        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        notification.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture()
        );

        assertEquals(data, captorData.getValue());
        assertEquals(TEST_CASE_ID, captorCaseId.getValue());
        assertEquals(data.getApplicant1(), captorApplicant.getValue());
        assertEquals(APPLICANT_1_TEST_PACK_INFO, captorDocumentPackInfo.getValue());
        assertEquals(LETTER_ID, captorLetterId.getValue());
    }

    @Test
    void shouldSendToApplicant2IfRegenerated() {
        CaseData data = caseData();
        when(conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1()))
            .thenReturn(APPLICANT_2_TEST_PACK_INFO);
        when(conditionalOrderGrantedDocumentPack.getLetterId()).thenReturn(LETTER_ID);
        data.getApplicant1().setCoPronouncedCoverLetterRegenerated(YES);

        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        notification.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture()
        );

        assertEquals(data, captorData.getValue());
        assertEquals(TEST_CASE_ID, captorCaseId.getValue());
        assertEquals(data.getApplicant1(), captorApplicant.getValue());
        assertEquals(APPLICANT_2_TEST_PACK_INFO, captorDocumentPackInfo.getValue());
        assertEquals(LETTER_ID, captorLetterId.getValue());
    }
}
