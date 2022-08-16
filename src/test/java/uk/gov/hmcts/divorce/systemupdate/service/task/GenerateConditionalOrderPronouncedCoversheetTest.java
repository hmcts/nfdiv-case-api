package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet.ADDRESS;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet.NAME;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet.PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GenerateConditionalOrderPronouncedCoversheetTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    GenerateConditionalOrderPronouncedCoversheet generateConditionalOrderPronouncedCoversheet;

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant1IfNotRepresentedAndPrintDocs() {

        setMockClock(clock);

        CaseData caseData = caseData();

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(NAME, "Bob Smith");
        applicant1TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant1TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant1TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(NAME, "Julie Smith");
        applicant2TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant2TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant2TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        CaseDetails<CaseData, State> details =
            CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        generateConditionalOrderPronouncedCoversheet.apply(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
        verifyNoMoreInteractions(caseDataDocumentService);
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
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .address("221B Baker Street,\n London,\n NW1 6XE\n")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(NAME, "App1 Sol");
        applicant1TemplateVars.put(ADDRESS, "5 The Street,\n London,\n W1 1BW");
        applicant1TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant1TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(NAME, "App2 Sol");
        applicant2TemplateVars.put(ADDRESS, "221B Baker Street,\n London,\n NW1 6XE\n");
        applicant2TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant2TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        CaseDetails<CaseData, State> details =
            CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        generateConditionalOrderPronouncedCoversheet.apply(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateDissolutionCoGrantedCoversheetContent() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "Bob Smith");
        templateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(IS_DIVORCE, false);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        templateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        CaseDetails<CaseData, State> details =
            CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        generateConditionalOrderPronouncedCoversheet.apply(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            templateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldNotGenerateCoversheetForApplicantsIfTheyAreOnlineApplicants() {

        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail("test@email.com");

        CaseDetails<CaseData, State> details =
            CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        generateConditionalOrderPronouncedCoversheet.apply(details);

        verifyNoInteractions(caseDataDocumentService);
    }

    private CaseData caseData() {
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
                    .documentsGenerated(new ArrayList<>())
                    .build()
            )
            .conditionalOrder(ConditionalOrder.builder().grantedDate(LocalDate.of(2022, 4, 28)).build())
            .build();
    }
}
