package uk.gov.hmcts.divorce.systemupdate.service.print;

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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

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
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_CAN_APPLY;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP2;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ApplyForFinalOrderPrinter.LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ApplyForFinalOrderPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @InjectMocks
    private ApplyForFinalOrderPrinter applyForFinalOrderPrinter;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintAwaitingFinalOrderPack() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_CAN_APPLY_APP1)
                .build())
            .build();

        final ListValue<DivorceDocument> d36Doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D36)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, d36Doc))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        applyForFinalOrderPrinter.sendLettersToApplicant1Offline(
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

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK);
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(d36Doc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldPrintAwaitingFinalOrderPackApplicant2InJointCase() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_CAN_APPLY_APP2)
                .build())
            .build();

        final ListValue<DivorceDocument> d36Doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D36)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().languagePreferenceWelsh(NO).build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, d36Doc))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        applyForFinalOrderPrinter.sendLettersToApplicant2Offline(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT),
            anyMap(),
            eq(ENGLISH)
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK);
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(d36Doc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldNotPrintIfAwaitingFinalOrderLettersNotFound() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(emptyList())
                    .build()
            )
            .build();

        applyForFinalOrderPrinter.sendLettersToApplicant1Offline(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintIfNumberOfAwaitingFinalOrderLettersDoesNotMatchExpectedDocumentsSize() {

        final ListValue<DivorceDocument> coversheetDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> coCanApplyDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_CAN_APPLY)
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
            .build();

        applyForFinalOrderPrinter.sendLettersToApplicant1Offline(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verifyNoInteractions(bulkPrintService);
    }
}
