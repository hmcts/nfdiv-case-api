package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_JS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.ConditionalOrderPronouncedCoverLetterHelper.ADDRESS;
import static uk.gov.hmcts.divorce.systemupdate.service.task.ConditionalOrderPronouncedCoverLetterHelper.NAME;
import static uk.gov.hmcts.divorce.systemupdate.service.task.ConditionalOrderPronouncedCoverLetterHelper.PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataCOPronounced;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class ConditionalOrderPronouncedCoverLetterHelperTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @Test
    void shouldGenerateConditionalOrderPronouncedCoversheet() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant1TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant1TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant1TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant1TemplateVars.put(NAME, "Bob Smith");
        applicant1TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant1TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant1TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheet(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateConditionalOrderPronouncedCoversheetToApplicant1Solicitor() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setApplicationType(JOINT_APPLICATION);

        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .address("221B Baker Street,\n London,\n NW1 6XE\n")
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant1TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant1TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant1TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant1TemplateVars.put(NAME, "App1 Sol");
        applicant1TemplateVars.put(ADDRESS, "221B Baker Street,\n London,\n NW1 6XE\n");
        applicant1TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant1TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheet(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToRespondent() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setApplicationType(SOLE_APPLICATION);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put("courtName", caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant2TemplateVars.put(NAME, "Lily Jones");
        applicant2TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant2TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant2TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put("dateOfHearing", "28 April 2021");
        applicant2TemplateVars.put("timeOfHearing", "1:00 am");
        applicant2TemplateVars.put("partner", "wife");

        when(commonContent.getPartner(any(), any(), any())).thenReturn("wife");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheetOfflineRespondent(caseData, TEST_CASE_ID, caseData.getApplicant2(),
            caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant2SolicitorIfRepresentedAndPrintDocs() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setApplicationType(JOINT_APPLICATION);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .address("221B Baker Street,\n London,\n NW1 6XE\n")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put("courtName", caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant2TemplateVars.put(NAME, "App2 Sol");
        applicant2TemplateVars.put(ADDRESS, "221B Baker Street,\n London,\n NW1 6XE\n");
        applicant2TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant2TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put("dateOfHearing", "28 April 2021");
        applicant2TemplateVars.put("timeOfHearing", "1:00 am");
        applicant2TemplateVars.put("partner", "wife");

        when(commonContent.getPartner(any(), any(), any())).thenReturn("wife");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheetOfflineRespondent(caseData, TEST_CASE_ID, caseData.getApplicant2(),
            caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateConditionalOrderPronouncedCoversheetForJudicialSeparation() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setIsJudicialSeparation(YES);
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant1TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant1TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant1TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant1TemplateVars.put(NAME, "Bob Smith");
        applicant1TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant1TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant1TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheet(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_JS_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToofflineRespondentInJudicialSeparation() {

        setMockClock(clock);

        CaseData caseData = buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE);
        caseData.setIsJudicialSeparation(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setSolicitorRepresented(NO);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put("courtName", caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant2TemplateVars.put(NAME, "Lily Jones");
        applicant2TemplateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
        applicant2TemplateVars.put(DATE, getExpectedLocalDate().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        applicant2TemplateVars.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put("dateOfHearing", "28 April 2021");
        applicant2TemplateVars.put("timeOfHearing", "1:00 am");
        applicant2TemplateVars.put("partner", "wife");

        when(commonContent.getPartner(any(), any(), any())).thenReturn("wife");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        coverLetterHelper.generateConditionalOrderPronouncedCoversheetOfflineRespondent(caseData, TEST_CASE_ID, caseData.getApplicant2(),
            caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CO_GRANTED_COVER_LETTER_JS_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verifyNoMoreInteractions(caseDataDocumentService);
    }
}
