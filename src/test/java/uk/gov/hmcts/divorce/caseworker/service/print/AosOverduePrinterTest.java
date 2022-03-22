package uk.gov.hmcts.divorce.caseworker.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class AosOverduePrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private AosOverduePrinter aosOverduePrinter;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    private static final LocalDate NOW = LocalDate.now();

    @Test
    void shouldPrintAosOverdueLetterForApplicantIfRequiredDocumentsArePresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(AOS_OVERDUE_LETTER)
                .documentDateAdded(NOW)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentDateAdded(NOW)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        aosOverduePrinter.sendLetterToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("aos-overdue");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
    }

    @Test
    void shouldNotPrintAosOverdueLetterIfRequiredDocumentsAreNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentDateAdded(NOW)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .documentDateAdded(NOW)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        aosOverduePrinter.sendLetterToApplicant(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }
}
