package uk.gov.hmcts.divorce.systemupdate.print;

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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter.ADDRESS;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter.LETTER_TYPE_CO_PRONOUNCED;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter.NAME;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter.PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ConditionalOrderPronouncedPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderPronouncedPrinter conditionalOrderPronouncedPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    private static final DivorceDocument coGrantedDocValue =
        DivorceDocument.builder()
            .documentType(CONDITIONAL_ORDER_GRANTED)
            .build();

    private static final DivorceDocument coGrantedCoversheetValue =
        DivorceDocument.builder()
            .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET)
            .build();

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant1IfNotRepresentedAndPrintDocs() {
        setMockClock(clock);

        CaseData caseData = caseData();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "Bob Smith");
        templateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, true);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, caseData.getApplicant1());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheetValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDocValue);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant2IfNotRepresentedAndPrintDocs() {
        setMockClock(clock);

        CaseData caseData = caseData();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "Julie Smith");
        templateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, true);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, caseData.getApplicant2());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheetValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDocValue);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant1SolicitorIfRepresentedAndPrintDocs() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .address("5 The Street,\n London,\n W1 1BW")
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "App1 Sol");
        templateVars.put(ADDRESS, "5 The Street,\n London,\n W1 1BW");
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, true);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, caseData.getApplicant1());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheetValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDocValue);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant2SolicitorIfRepresentedAndPrintDocs() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .address("221B Baker Street,\n London,\n NW1 6XE\n")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "App2 Sol");
        templateVars.put(ADDRESS, "221B Baker Street,\n London,\n NW1 6XE\n");
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, true);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, caseData.getApplicant2());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheetValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDocValue);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldGenerateDissolutionCoGrantedCoversheetContent() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "Bob Smith");
        templateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, false);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    private CaseData caseData() {
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(coGrantedDocValue)
            .build();

        final ListValue<DivorceDocument> coGrantedCoversheet = ListValue.<DivorceDocument>builder()
            .value(coGrantedCoversheetValue)
            .build();

        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(
                Applicant.builder()
                    .firstName("Julie")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coGrantedDoc, coGrantedCoversheet))
                    .build()
            )
            .conditionalOrder(ConditionalOrder.builder().grantedDate(LocalDate.of(2022, 4, 28)).build())
            .build();
    }
}
