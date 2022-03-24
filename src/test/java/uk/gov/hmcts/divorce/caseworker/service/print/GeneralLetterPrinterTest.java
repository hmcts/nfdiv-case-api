package uk.gov.hmcts.divorce.caseworker.service.print;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
class GeneralLetterPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private GeneralLetterPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintGeneralLetterIfRequiredDocumentsArePresent() {

        Document generalLetter = Document.builder()
            .filename("GeneralLetter.pdf")
            .build();

//        Document attachment = Document.builder()
//            .filename("some-attachment.pdf")
//            .build();

//        ListValue<Document> generalLetter = ListValue.<Document>builder()
//            .value(Document.builder()
//                .filename("GeneralLetter.pdf")
//                .build())
//            .build();

        ListValue<Document> attachment = ListValue.<Document>builder()
            .value(Document.builder()
                .filename("some-attachment.pdf")
                .build())
            .build();

        final ListValue<GeneralLetterDetails> doc1 = ListValue.<GeneralLetterDetails>builder()
            .value(GeneralLetterDetails.builder()
                    .generalLetterLink(generalLetter)
                    .generalLetterAttachmentLinks(Lists.newArrayList(attachment))
                    .build())
            .build();

        final ListValue<GeneralLetterDetails> doc2 = ListValue.<GeneralLetterDetails>builder()
            .value(GeneralLetterDetails.builder()
                .generalLetterLink(Document.builder().build())
                .build())
            .build();

        final CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);

        caseData.setGeneralLetters(Lists.newArrayList(doc1, doc2));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.sendLetterWithAttachments(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("general-letter");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument().getDocumentLink()).isSameAs(generalLetter);
        assertThat(print.getLetters().get(1).getDivorceDocument().getDocumentLink()).isSameAs(attachment);
    }

    @Test
    void shouldNotPrintGeneralLetterIfRequiredDocumentsAreNotPresent() {

        printer.sendLetterWithAttachments(CaseData.builder().build(), TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }
}
