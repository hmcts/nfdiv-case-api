package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForClarificationCoverLetter;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.task.GenerateCoRefusedCoverLetter;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class AwaitingClarificationApplicationPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Mock
    private GenerateJudicialSeparationCORefusedForClarificationCoverLetter generateJudicialSeparationCORefusedForClarificationCoverLetter;

    @InjectMocks
    private AwaitingClarificationApplicationPrinter awaitingClarificationApplicationPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintAwaitingClarificationPack() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER)
                .build())
            .build();

        final ListValue<DivorceDocument> coRefusalDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REFUSAL)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .application(Application.builder().newPaperCase(NO).build())
            .isJudicialSeparation(NO)
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, coRefusalDoc))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());
        when(generateJudicialSeparationCORefusedForClarificationCoverLetter.getDocumentType(caseData, caseData.getApplicant1()))
            .thenReturn(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(generateCoRefusedCoverLetter).generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("awaiting-clarification-application-letter");
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(coRefusalDoc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldNotPrintIfAwaitingClarificationLettersNotFound() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .application(Application.builder().newPaperCase(NO).build())
            .isJudicialSeparation(NO)
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(emptyList())
                    .build()
            )
            .build();

        when(generateJudicialSeparationCORefusedForClarificationCoverLetter.getDocumentType(caseData, caseData.getApplicant1()))
            .thenReturn(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintIfNumberOfAwaitingClarificationLettersDoesNotMatchExpectedDocumentsSize() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .application(Application.builder().newPaperCase(NO).build())
            .isJudicialSeparation(NO)
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc))
                    .build()
            )
            .build();

        when(generateJudicialSeparationCORefusedForClarificationCoverLetter.getDocumentType(caseData, caseData.getApplicant1()))
            .thenReturn(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(generateCoRefusedCoverLetter).generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verifyNoInteractions(bulkPrintService);
    }
}
