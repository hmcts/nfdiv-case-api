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
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;

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
import static uk.gov.hmcts.divorce.caseworker.service.print.AppliedForCoPrinter.NAME;
import static uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter.CIVIL_PARTNERSHIP_LEGALLY_ENDED;
import static uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter.DIVORCED_OR_CP_LEGALLY_ENDED;
import static uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter.END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter.GET_A_DIVORCE;
import static uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter.YOU_ARE_DIVORCED;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class SwitchToSoleCoPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private SwitchToSoleCoPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldGenerateAndPrintSwitchToSoleCoLetterWithDivorceContent() {

        setMockClock(clock);

        final ListValue<DivorceDocument> switchToSoleCoLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(SWITCH_TO_SOLE_CO_LETTER)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(switchToSoleCoLetter))
                    .build()
            )
            .build();

        final Applicant applicant = Applicant.builder()
            .gender(MALE)
            .build();

        final Applicant respondent = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(APPLICANT_ADDRESS)
            .languagePreferenceWelsh(NO)
            .build();

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(APPLICANT_ADDRESS));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, "husband");
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, YOU_ARE_DIVORCED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateContent.put(THE_APPLICATION, DIVORCE);

        when(commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()))
            .thenReturn("husband");
        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, applicant, respondent);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("switch-to-sole-co-letter");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(switchToSoleCoLetter.getValue());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            templateContent,
            TEST_CASE_ID,
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldGenerateAndPrintSwitchToSoleCoLetterWithCivilPartnershipContent() {

        setMockClock(clock);

        final ListValue<DivorceDocument> switchToSoleCoLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(SWITCH_TO_SOLE_CO_LETTER)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(switchToSoleCoLetter))
                    .build()
            )
            .build();

        final Applicant applicant = Applicant.builder()
            .gender(MALE)
            .build();

        final Applicant respondent = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(APPLICANT_ADDRESS)
            .languagePreferenceWelsh(NO)
            .build();

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(APPLICANT_ADDRESS));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, "husband");
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, CIVIL_PARTNERSHIP_LEGALLY_ENDED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        templateContent.put(THE_APPLICATION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);

        when(commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()))
            .thenReturn("husband");
        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, applicant, respondent);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("switch-to-sole-co-letter");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(switchToSoleCoLetter.getValue());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            templateContent,
            TEST_CASE_ID,
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldNotPrintSwitchToSoleCoLetterIfRequiredDocumentNotPresent() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .documents(CaseDocuments.builder().build())
            .build();

        final Applicant applicant = Applicant.builder()
            .gender(MALE)
            .build();

        final Applicant respondent = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(APPLICANT_ADDRESS)
            .languagePreferenceWelsh(NO)
            .build();

        printer.print(caseData, TEST_CASE_ID, applicant, respondent);

        verifyNoInteractions(bulkPrintService);
    }
}
