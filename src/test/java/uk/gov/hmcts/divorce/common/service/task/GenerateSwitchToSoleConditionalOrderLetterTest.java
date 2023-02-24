package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter.CIVIL_PARTNERSHIP_LEGALLY_ENDED;
import static uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter.DIVORCED_OR_CP_LEGALLY_ENDED;
import static uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter.END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter.GET_A_DIVORCE;
import static uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter.YOU_ARE_DIVORCED;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class GenerateSwitchToSoleConditionalOrderLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private GenerateSwitchToSoleConditionalOrderLetter generateSwitchToSoleConditionalOrderLetter;

    @Test
    void shouldGenerateSwitchToSoleCoLetterWithDivorceContent() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
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
        templateContent.put(FIRST_NAME, respondent.getFirstName());
        templateContent.put(LAST_NAME, respondent.getLastName());
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(APPLICANT_ADDRESS));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, "husband");
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, YOU_ARE_DIVORCED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateContent.put(THE_APPLICATION, DIVORCE);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));
        when(commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()))
            .thenReturn("husband");

        generateSwitchToSoleConditionalOrderLetter.apply(caseData, TEST_CASE_ID, applicant, respondent);

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
    void shouldGenerateSwitchToSoleCoLetterWithCivilPartnershipContent() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
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
        templateContent.put(FIRST_NAME, respondent.getFirstName());
        templateContent.put(LAST_NAME, respondent.getLastName());
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(APPLICANT_ADDRESS));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, "civil partner");
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, CIVIL_PARTNERSHIP_LEGALLY_ENDED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        templateContent.put(THE_APPLICATION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));
        when(commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()))
            .thenReturn("civil partner");

        generateSwitchToSoleConditionalOrderLetter.apply(caseData, TEST_CASE_ID, applicant, respondent);

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
}
