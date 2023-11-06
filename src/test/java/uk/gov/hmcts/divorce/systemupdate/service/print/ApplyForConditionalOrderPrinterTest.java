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
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForConditionalOrderDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

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
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_CAN_APPLY;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ApplyForConditionalOrderPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private GenerateD84Form generateD84Form;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private GenerateApplyForConditionalOrderDocument generateApplyForConditionalOrderDocument;

    @InjectMocks
    private ApplyForConditionalOrderPrinter applyForConditionalOrderPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintAwaitingConditionalOrderPack() {

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

        final ListValue<DivorceDocument> d84Doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D84)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coversheetDoc, coCanApplyDoc, d84Doc))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        applyForConditionalOrderPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(generateD84Form).generateD84Document(caseData, TEST_CASE_ID);

        verify(generateApplyForConditionalOrderDocument).generateApplyForConditionalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("apply-for-conditional-order-pack");
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDoc.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coCanApplyDoc.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(d84Doc.getValue());

        verify(bulkPrintService).print(print);
    }

    @Test
    void shouldNotPrintIfAwaitingConditionalOrderLettersNotFound() {

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

        applyForConditionalOrderPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintIfNumberOfAwaitingConditionalOrderLettersDoesNotMatchExpectedDocumentsSize() {

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

        applyForConditionalOrderPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verify(generateCoversheet).generateCoversheet(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(COVERSHEET_APPLICANT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(generateD84Form).generateD84Document(caseData, TEST_CASE_ID);

        verify(generateApplyForConditionalOrderDocument).generateApplyForConditionalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verifyNoInteractions(bulkPrintService);
    }
}
