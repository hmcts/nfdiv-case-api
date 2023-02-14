package uk.gov.hmcts.divorce.caseworker.service.print;

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

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class RegenerateCourtOrdersPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private RegenerateCourtOrdersPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintRegenerateCourtOrdersIfDocumentsArePresent() {

        final ListValue<DivorceDocument> coGrantedCoversheet = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        final ListValue<DivorceDocument> foGrantedCoverLetter = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> foGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_GRANTED)
                .build())
            .build();

        final ListValue<DivorceDocument> coeCoverLetter = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
                .build())
            .build();

        final ListValue<DivorceDocument> coeDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CERTIFICATE_OF_ENTITLEMENT)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(
                        asList(
                            coGrantedCoversheet,
                            coGrantedDoc,
                            foGrantedCoverLetter,
                            foGrantedDoc,
                            coeCoverLetter,
                            coeDoc)
                    )
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, true);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("regenerate-court-orders-letter");
        assertThat(print.getLetters().size()).isEqualTo(6);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheet.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(foGrantedCoverLetter.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(foGrantedDoc.getValue());
        assertThat(print.getLetters().get(4).getDivorceDocument()).isSameAs(coeCoverLetter.getValue());
        assertThat(print.getLetters().get(5).getDivorceDocument()).isSameAs(coeDoc.getValue());
    }

    @Test
    void shouldNotPrintGeneralLetterIfRequiredDocumentsAreNotPresent() {
        printer.print(CaseData.builder().build(), TEST_CASE_ID, true);
        verifyNoInteractions(bulkPrintService);
    }
}
