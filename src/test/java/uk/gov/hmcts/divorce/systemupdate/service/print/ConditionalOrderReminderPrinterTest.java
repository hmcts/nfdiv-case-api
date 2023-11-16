package uk.gov.hmcts.divorce.systemupdate.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REMINDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderReminderPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private ConditionalOrderReminderPrinter conditionalOrderReminderPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintConditionalOrderReminderPack() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REMINDER)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D84)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2, doc3)).build())
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        conditionalOrderReminderPrinter.sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("conditional-order-reminder-pack");
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldNotPrintConditionalOrderReminderPackWhenMissingDocumentCoversheet() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REMINDER)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D84)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        conditionalOrderReminderPrinter.sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2());

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintConditionalOrderReminderPackWhenMissingDocumentCoversheetApp1() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REMINDER)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D84)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .email("testresp@test.com")
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        conditionalOrderReminderPrinter.sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verifyNoInteractions(bulkPrintService);
    }
}
