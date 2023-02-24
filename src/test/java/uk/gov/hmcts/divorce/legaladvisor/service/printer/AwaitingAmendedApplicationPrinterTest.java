package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForAmendmentCoverLetter;
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
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class AwaitingAmendedApplicationPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    @Mock
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Mock
    private GenerateJudicialSeparationCORefusedForAmendmentCoverLetter generateJudicialSeparationCORefusedForAmendmentCoverLetter;

    @InjectMocks
    private AwaitingAmendedApplicationPrinter awaitingAmendedApplicationPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintAwaitingAmendedApplicationPack() {

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

        final ListValue<DivorceDocument> applicationDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, coRefusalDoc, applicationDoc))
                    .build()
            )
            .isJudicialSeparation(YesOrNo.NO)
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        awaitingAmendedApplicationPrinter.sendLetters(
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
            REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("awaiting-amended-application-letter");
        assertThat(print.getLetters().size()).isEqualTo(4);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(coRefusalDoc.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(applicationDoc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldPrintAwaitingAmendedApplicationPackForJudicialSeparation() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER)
                .build())
            .build();

        final ListValue<DivorceDocument> coRefusalDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REFUSAL)
                .build())
            .build();

        final ListValue<DivorceDocument> applicationDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, coRefusalDoc, applicationDoc))
                    .build()
            )
            .isJudicialSeparation(YesOrNo.YES)
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        awaitingAmendedApplicationPrinter.sendLetters(
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

        verify(generateJudicialSeparationCORefusedForAmendmentCoverLetter).generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("awaiting-amended-application-letter");
        assertThat(print.getLetters()).hasSize(4);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(coRefusalDoc.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(applicationDoc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldPrintAwaitingAmendedApplicationSolicitorPackForJudicialSeparation() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER)
                .build())
            .build();

        final ListValue<DivorceDocument> coRefusalDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_REFUSAL)
                .build())
            .build();

        final ListValue<DivorceDocument> applicationDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().solicitorRepresented(YesOrNo.YES).languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, coRefusalDoc, applicationDoc))
                    .build()
            )
            .application(Application.builder().newPaperCase(YesOrNo.YES).build())
            .isJudicialSeparation(YesOrNo.YES)
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        awaitingAmendedApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT2_SOLICITOR),
            anyMap(),
            eq(ENGLISH)
        );

        verify(generateJudicialSeparationCORefusedForAmendmentCoverLetter).generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("awaiting-amended-application-letter");
        assertThat(print.getLetters()).hasSize(4);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(coRefusalDoc.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(applicationDoc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldNotPrintIfAwaitingAmendedApplicationLettersNotFound() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(emptyList())
                    .build()
            )
            .isJudicialSeparation(YesOrNo.NO)
            .build();

        awaitingAmendedApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintIfNumberOfAwaitingAmendedApplicationLettersDoesNotMatchExpectedDocumentsSize() {

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
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc))
                    .build()
            )
            .isJudicialSeparation(YesOrNo.NO)
            .build();

        awaitingAmendedApplicationPrinter.sendLetters(
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
            REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        verifyNoInteractions(bulkPrintService);
    }
}
