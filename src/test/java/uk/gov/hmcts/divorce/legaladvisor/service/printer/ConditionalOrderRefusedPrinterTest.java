package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.CoRefusalDocumentPack;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.ConditionalOrderRefusedDocumentGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE;
import static uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ConditionalOrderRefusedPrinterTest {

    @Mock
    private ConditionalOrderRefusedDocumentGenerator conditionalOrderRefusedDocumentGenerator;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private ConditionalOrderRefusedPrinter conditionalOrderRefusedPrinter;

    @Captor
    private ArgumentCaptor<Print> printArgumentCaptor;

    @Test
    void shouldPrintAwaitingAmendedApplicationPack() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.REJECT)
                .build())
            .supplementaryCaseType(NA)
            .build();

        final List<Letter> amendmentRefusalLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.AMENDMENT_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData,
            TEST_CASE_ID, caseData.getApplicant1(), CoRefusalDocumentPack.AMENDMENT_PACK)).thenReturn(amendmentRefusalLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(amendmentRefusalLetters);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.AMENDMENT_PACK.getLetterType().toString());
    }

    @Test
    void shouldPrintAwaitingAmendedApplicationPackForJudicialSeparation() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.REJECT)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        final List<Letter> jsAmendmentRefusalLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.AMENDMENT_JS_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData,
            TEST_CASE_ID, caseData.getApplicant1(), CoRefusalDocumentPack.AMENDMENT_JS_PACK)).thenReturn(jsAmendmentRefusalLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(jsAmendmentRefusalLetters);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.AMENDMENT_JS_PACK.getLetterType().toString());
    }

    @Test
    void shouldPrintAwaitingAmendedApplicationSolicitorPackForJudicialSeparation() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.REJECT)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        final List<Letter> jsSolAmendmentPack = getLettersFromDocumentPackSet(CoRefusalDocumentPack.AMENDMENT_JS_SOL_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CoRefusalDocumentPack.AMENDMENT_JS_SOL_PACK)).thenReturn(jsSolAmendmentPack);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(jsSolAmendmentPack);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.AMENDMENT_JS_SOL_PACK.getLetterType().toString());

    }

    @Test
    void shouldPrintAwaitingClarificationApplicationPack() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.MORE_INFO)
                .build())
            .supplementaryCaseType(NA)
            .build();

        List<Letter> clarificationLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.CLARIFICATION_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CoRefusalDocumentPack.CLARIFICATION_PACK)).thenReturn(clarificationLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(clarificationLetters);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.CLARIFICATION_PACK.getLetterType().toString());
    }

    @Test
    void shouldPrintAwaitingClarificationApplicationPackForJudicialSeparation() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.MORE_INFO)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        List<Letter> jsClarificationLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.CLARIFICATION_JS_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CoRefusalDocumentPack.CLARIFICATION_JS_PACK)).thenReturn(jsClarificationLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(jsClarificationLetters);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.CLARIFICATION_JS_PACK.getLetterType().toString());
    }

    @Test
    void shouldPrintAwaitingClarificationApplicationSolicitorPackForJudicialSeparation() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.MORE_INFO)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        List<Letter> jsSolClarificationLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.CLARIFICATION_SOL_JS_PACK);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CoRefusalDocumentPack.CLARIFICATION_SOL_JS_PACK)).thenReturn(jsSolClarificationLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        final Print print = printArgumentCaptor.getValue();

        assertThat(print.getLetters()).containsAll(jsSolClarificationLetters);
        assertThat(print.getLetterType()).isEqualTo(CoRefusalDocumentPack.CLARIFICATION_SOL_JS_PACK.getLetterType().toString());
    }

    @Test
    void shouldNotPrintWhenLettersAreMissing() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.MORE_INFO)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        List<Letter> jsSolClarificationLetters = getLettersFromDocumentPackSet(CoRefusalDocumentPack.CLARIFICATION_SOL_JS_PACK);
        jsSolClarificationLetters.remove(0);

        when(conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CoRefusalDocumentPack.CLARIFICATION_SOL_JS_PACK)).thenReturn(jsSolClarificationLetters);

        conditionalOrderRefusedPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verifyNoInteractions(bulkPrintService);
    }

    private List<Letter> getLettersFromDocumentPackSet(CoRefusalDocumentPack documentPack) {
        return documentPack.getDocumentPack().stream()
            .map(documentType -> new Letter(Document.builder().filename(documentType.toString()).build(), 1))
            .toList();
    }
}
