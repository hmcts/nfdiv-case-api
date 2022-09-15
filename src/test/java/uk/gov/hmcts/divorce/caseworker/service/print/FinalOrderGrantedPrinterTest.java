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

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class FinalOrderGrantedPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private FinalOrderGrantedPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldGenerateFinalOrderAndUpdateCaseData() {

        final ListValue<DivorceDocument> finalOrderGrantedLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(FINAL_ORDER_GRANTED)
                    .build())
                .build();

        final ListValue<DivorceDocument> finalOrderGrantedCoverLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(List.of(finalOrderGrantedLetter, finalOrderGrantedCoverLetter))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, FINAL_ORDER_GRANTED_COVER_LETTER_APP_1);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("final-order-granted-letter");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(finalOrderGrantedCoverLetter.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(finalOrderGrantedLetter.getValue());
    }

    @Test
    void shouldNotPrintFinalOrderGrantedLetterIfRequiredDocumentNotPresent() {

        final ListValue<DivorceDocument> finalOrderGrantedCoverLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(finalOrderGrantedCoverLetter))
                    .build()
            )
            .build();

        printer.print(caseData, TEST_CASE_ID, FINAL_ORDER_GRANTED_COVER_LETTER_APP_1);

        verifyNoInteractions(bulkPrintService);
    }
}
