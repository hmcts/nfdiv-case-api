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
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.service.print.AppliedForCoPrinter.DATE_D84_RECEIVED;
import static uk.gov.hmcts.divorce.caseworker.service.print.AppliedForCoPrinter.GRANTED_DATE;
import static uk.gov.hmcts.divorce.caseworker.service.print.AppliedForCoPrinter.NAME;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLIED_FOR_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class AppliedForCoPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private AppliedForCoPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldGenerateAndPrintAppliedForConditionalOrderLetter() {

        setMockClock(clock);

        final ListValue<DivorceDocument> appliedForCoLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(APPLIED_FOR_CO_LETTER)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateD84FormScanned(now(clock).minusWeeks(1))
                    .build()
            )
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(appliedForCoLetter))
                    .build()
            )
            .build();

        final Applicant applicant = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(APPLICANT_ADDRESS)
            .languagePreferenceWelsh(NO)
            .build();

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(DATE_D84_RECEIVED, caseData.getConditionalOrder().getDateD84FormScanned().format(DATE_TIME_FORMATTER));
        templateContent.put(GRANTED_DATE, now(clock).plusWeeks(4).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, applicant);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applied-for-co-letter");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(appliedForCoLetter.getValue());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            APPLIED_FOR_CO_LETTER,
            templateContent,
            TEST_CASE_ID,
            APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldNotPrintAppliedForConditionalOrderLetterIfRequiredDocumentNotPresent() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateD84FormScanned(now(clock).minusWeeks(1))
                    .build()
            )
            .build();

        final Applicant applicant = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(APPLICANT_ADDRESS)
            .languagePreferenceWelsh(NO)
            .build();

        printer.print(caseData, TEST_CASE_ID, applicant);

        verifyNoInteractions(bulkPrintService);
    }
}
