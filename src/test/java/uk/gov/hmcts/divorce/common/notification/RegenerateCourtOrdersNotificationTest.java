package uk.gov.hmcts.divorce.common.notification;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.FinalOrderGrantedDocumentPack;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RegenerateCourtOrdersNotificationTest {

    @Mock
    private LetterPrinter letterPrinter;

    @Mock
    private CertificateOfEntitlementDocumentPack certificateOfEntitlementDocPack;

    @Mock
    private FinalOrderGrantedDocumentPack finalOrderGrantedDocPack;

    @Mock
    private ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocPack;

    @InjectMocks
    private RegenerateCourtOrdersNotification notification;

    private static final DocumentPackInfo APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, Optional.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID),
            CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
        ),
        ImmutableMap.of(
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2, Optional.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID),
            CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
        ),
        ImmutableMap.of(
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_CONDITIONAL_ORDER_PRONOUNCED_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_FINAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, Optional.of(FINAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            FINAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID, FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        )
    );

    public static final String LETTER_ID = "letterId";

    @Test
    void testSendToApplicant1Offline() {
        // Prepare test data
        CaseData caseData = new CaseData();

        ListValue<DivorceDocument> conditionalOrderGranted = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        ListValue<DivorceDocument> finalOrderDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_GRANTED)
                .build())
            .build();

        ListValue<DivorceDocument> certificateOfEntitlement = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CERTIFICATE_OF_ENTITLEMENT)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(conditionalOrderGranted, finalOrderDocument, certificateOfEntitlement))
            .build());

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .certificateOfEntitlementDocument(certificateOfEntitlement.getValue())
            .build());

        when(conditionalOrderPronouncedDocPack.getDocumentPack(caseData, caseData.getApplicant2()))
            .thenReturn(APPLICANT_1_CONDITIONAL_ORDER_PRONOUNCED_PACK);
        when(conditionalOrderPronouncedDocPack.getLetterId()).thenReturn(LETTER_ID);

        when(finalOrderGrantedDocPack.getDocumentPack(caseData, caseData.getApplicant2()))
            .thenReturn(APPLICANT_1_FINAL_ORDER_PACK);
        when(finalOrderGrantedDocPack.getLetterId()).thenReturn(LETTER_ID);
        when(certificateOfEntitlementDocPack.getDocumentPack(caseData, caseData.getApplicant1()))
            .thenReturn(APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK);
        when(certificateOfEntitlementDocPack.getLetterId()).thenReturn(LETTER_ID);

        when(certificateOfEntitlementDocPack.getDocumentPack(caseData, caseData.getApplicant1()))
            .thenReturn(APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK);
        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        // Call the method under test
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        // Verify that the letterPrinter's sendLetters method was called with the correct arguments
        verify(letterPrinter, times(3)).sendLetters(captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture());
        Assertions.assertEquals(caseData, captorData.getAllValues().get(0));
        Assertions.assertEquals(TEST_CASE_ID, captorCaseId.getAllValues().get(0));
        Assertions.assertEquals(caseData.getApplicant1(), captorApplicant.getAllValues().get(0));
        Assertions.assertEquals(APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK, captorDocumentPackInfo.getAllValues().get(0));
        Assertions.assertEquals(LETTER_ID, captorLetterId.getAllValues().get(0));
    }

    @Test
    void testSendToApplicant2Offline() {
        CaseData caseData = caseData();

        ListValue<DivorceDocument> conditionalOrderGranted = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        ListValue<DivorceDocument> finalOrderDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_GRANTED)
                .build())
            .build();

        ListValue<DivorceDocument> certificateOfEntitlement = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CERTIFICATE_OF_ENTITLEMENT)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(conditionalOrderGranted, finalOrderDocument, certificateOfEntitlement))
            .build());

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .certificateOfEntitlementDocument(certificateOfEntitlement.getValue())
            .build());

        when(conditionalOrderPronouncedDocPack.getDocumentPack(caseData, caseData.getApplicant2()))
            .thenReturn(APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK);
        when(conditionalOrderPronouncedDocPack.getLetterId()).thenReturn(LETTER_ID);

        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(letterPrinter, times(3)).sendLetters(
            captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture()
        );

        Assertions.assertEquals(caseData, captorData.getValue());
        Assertions.assertEquals(TEST_CASE_ID, captorCaseId.getValue());
        Assertions.assertEquals(caseData.getApplicant2(), captorApplicant.getValue());
        Assertions.assertEquals(APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK, captorDocumentPackInfo.getValue());
        Assertions.assertEquals(LETTER_ID, captorLetterId.getValue());
    }

    @Test
    void shouldNotSendIfDocumentIsNotPresentOnCaseToOfflineApplicant1() {
        // Prepare test data, where the document is not present on the case
        CaseData caseData = new CaseData();

        // Call the method under test
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        // Verify that the letterPrinter is not called
        verifyNoInteractions(letterPrinter);
    }

    @Test
    void shouldNotSendIfDocumentIsNotPresentOnCaseToOfflineApplicant2() {
        // Prepare test data, where the document is not present on the case
        CaseData caseData = new CaseData();

        // Call the method under test
        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        // Verify that the letterPrinter is not called
        verifyNoInteractions(letterPrinter);
    }
}

