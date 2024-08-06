package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getTemplateFormatDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPronouncedTemplateContentTest {

    @Mock
    Clock clock;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conditionalOrderPronouncedTemplateContent, "finalOrderOffsetDays", 43);
    }

    @Test
    void shouldApplyEnglishDivorceContentFromCaseDataForConditionalOrderPronouncedTemplate() {

        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();
        basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(commonContent.getPartner(any(), any(), eq(ENGLISH))).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(basicDocmosisTemplateContent);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        LocalDate coPronouncedDate = LocalDate.of(2022, 6, 10);
        LocalDate marriageDate = LocalDate.of(2000, 1, 2);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .placeOfMarriage("London")
                    .countryOfMarriage("UK")
                    .date(marriageDate)
                    .build())
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(getExpectedLocalDateTime())
                .court(BIRMINGHAM)
                .grantedDate(coPronouncedDate)
                .pronouncementJudge("District Judge")
                .build())
            .build();

        final Map<String, Object> result =
            conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicant1);

        assertThat(result).contains(
            entry(IS_SOLE, true),
            entry(IS_DIVORCE, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DOCUMENTS_ISSUED_ON, getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(PARTNER, "husband"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "UK"),
            entry(MARRIAGE_DATE, "2 January 2000"),
            entry(JUDGE_NAME, "District Judge"),
            entry(COURT_NAME, "Birmingham Civil and Family Justice Centre"),
            entry(CO_PRONOUNCED_DATE, "10 June 2022"),
            entry(DATE_FO_ELIGIBLE_FROM, "23 July 2022"),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(DocmosisTemplateConstants.CASE_REFERENCE, "1616-5914-0147-3378"),
            entry(DATE_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
            entry(TIME_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER)),
            entry(CommonContent.NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME)
        );
    }

    @Test
    void shouldApplyWelshDivorceContentFromCaseDataForConditionalOrderPronouncedTemplate() {

        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();
        basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
        basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY);
        basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);

        when(commonContent.getPartner(any(), any(), eq(WELSH))).thenReturn("gŵr");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(basicDocmosisTemplateContent);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.YES)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        LocalDate coPronouncedDate = LocalDate.of(2022, 6, 10);
        LocalDate marriageDate = LocalDate.of(2000, 1, 2);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .placeOfMarriage("London")
                    .countryOfMarriage("UK")
                    .date(marriageDate)
                    .build())
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(getExpectedLocalDateTime())
                .court(BIRMINGHAM)
                .grantedDate(coPronouncedDate)
                .pronouncementJudge("District Judge")
                .build())
            .build();

        final Map<String, Object> result =
            conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicant1);

        assertThat(result).contains(
            entry(IS_SOLE, true),
            entry(IS_DIVORCE, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DOCUMENTS_ISSUED_ON, getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "briodas"),
            entry(PARTNER, "gŵr"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "UK"),
            entry(MARRIAGE_DATE, "2 January 2000"),
            entry(JUDGE_NAME, "District Judge"),
            entry(COURT_NAME, "Birmingham Civil and Family Justice Centre"),
            entry(CO_PRONOUNCED_DATE, "10 June 2022"),
            entry(DATE_FO_ELIGIBLE_FROM, "23 July 2022"),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(DocmosisTemplateConstants.CASE_REFERENCE, "1616-5914-0147-3378"),
            entry(DATE_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
            entry(TIME_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER)),
            entry(CommonContent.NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME)
        );

    }

    @Test
    void shouldApplyCoGrantedCoverLetterIdContent() {
        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();
        basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(commonContent.getPartner(any(), any(), eq(ENGLISH))).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(basicDocmosisTemplateContent);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        LocalDate coPronouncedDate = LocalDate.of(2022, 6, 10);
        LocalDate marriageDate = LocalDate.of(2000, 1, 2);

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .placeOfMarriage("London")
                    .countryOfMarriage("UK")
                    .date(marriageDate)
                    .build())
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(getExpectedLocalDateTime())
                .court(BIRMINGHAM)
                .grantedDate(coPronouncedDate)
                .pronouncementJudge("District Judge")
                .build())
            .build();

        final Map<String, Object> result =
            conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicant1);

        assertThat(result).contains(
            entry(IS_SOLE, false),
            entry(IS_DIVORCE, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DOCUMENTS_ISSUED_ON, getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(PARTNER, "husband"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "UK"),
            entry(MARRIAGE_DATE, "2 January 2000"),
            entry(JUDGE_NAME, "District Judge"),
            entry(COURT_NAME, "Birmingham Civil and Family Justice Centre"),
            entry(CO_PRONOUNCED_DATE, "10 June 2022"),
            entry(DATE_FO_ELIGIBLE_FROM, "23 July 2022"),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(DocmosisTemplateConstants.CASE_REFERENCE, "1616-5914-0147-3378"),
            entry(DATE_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
            entry(TIME_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER)),
            entry(CommonContent.NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME)
        );

    }

    @Test
    void templateVarsForJSSolicitorShouldUpdateTemplateContentCorrectly() {
        CaseData caseData = buildCaseDataWithSolicitor();

        Map<String, Object> templateContentMap = new HashMap<>();
        conditionalOrderPronouncedTemplateContent.templateVarsForJSSolicitor(
            templateContentMap, caseData, caseData.getApplicant1());

        assertThat(templateContentMap).contains(
            entry(SOLICITOR_NAME, "Solicitor Name"),
            entry(SOLICITOR_FIRM, "Solicitor Firm"),
            entry(SOLICITOR_ADDRESS, "Solicitor Address"),
            entry(IS_JOINT, true),
            entry(APPLICANT_1_FULL_NAME, "Applicant 1 Full Name"),
            entry(APPLICANT_2_FULL_NAME, "Applicant 2 Full Name"),
            entry(APPLICANT_1_SOLICITOR_NAME, "Solicitor Name"),
            entry(APPLICANT_2_SOLICITOR_NAME, "Not represented"),
            entry(SOLICITOR_REFERENCE, "Solicitor Ref")
        );
    }

    private CaseData buildCaseDataWithSolicitor() {
        Applicant applicant1 = Applicant.builder()
            .firstName("Applicant 1")
            .lastName("Full Name")
            .solicitor(Solicitor.builder()
                .name("Solicitor Name")
                .firmName("Solicitor Firm")
                .address("Solicitor Address")
                .reference("Solicitor Ref")
                .build())
            .solicitorRepresented(YES)
            .build();

        Applicant applicant2 = Applicant.builder()
            .firstName("Applicant 2")
            .lastName("Full Name")
            .build();

        return CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();
    }

    @Test
    void shouldCallTemplateVarsForJSSolicitorForJudicialSeparationCaseWithoutRepresentation() {
        CaseData caseData = buildCaseDataWithSolicitor();

        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        Applicant representedApplicant = caseData.getApplicant1();
        representedApplicant.setLanguagePreferenceWelsh(NO);

        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();
        basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(commonContent.getPartner(any(), any(), eq(ENGLISH))).thenReturn("civil partner");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(basicDocmosisTemplateContent);

        Map<String, Object> result = conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, representedApplicant);

        assertThat(result).contains(
            entry(IS_JOINT, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FULL_NAME, representedApplicant.getFullName()),
            entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
            entry(APPLICANT_1_SOLICITOR_NAME, "Solicitor Name"),
            entry(APPLICANT_2_SOLICITOR_NAME, "Not represented"),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(DocmosisTemplateConstants.CASE_REFERENCE, "1616-5914-0147-3378")
        );
    }

    @Test
    void shouldApplyProperPartnerLabelForApp2ConditionalOrderPronouncedTemplate() {

        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();
        basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .gender(Gender.MALE)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .gender(Gender.FEMALE)
            .build();

        when(commonContent.getPartner(any(), any(), eq(ENGLISH))).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(basicDocmosisTemplateContent);

        LocalDate coPronouncedDate = LocalDate.of(2022, 6, 10);
        LocalDate marriageDate = LocalDate.of(2000, 1, 2);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .placeOfMarriage("London")
                    .countryOfMarriage("UK")
                    .date(marriageDate)
                    .build())
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(getExpectedLocalDateTime())
                .court(BIRMINGHAM)
                .grantedDate(coPronouncedDate)
                .pronouncementJudge("District Judge")
                .build())
            .build();

        final Map<String, Object> result =
            conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicant2);

        assertThat(result).contains(
            entry(IS_SOLE, true),
            entry(IS_DIVORCE, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DOCUMENTS_ISSUED_ON, getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(PARTNER, "husband"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "UK"),
            entry(MARRIAGE_DATE, "2 January 2000"),
            entry(JUDGE_NAME, "District Judge"),
            entry(COURT_NAME, "Birmingham Civil and Family Justice Centre"),
            entry(CO_PRONOUNCED_DATE, "10 June 2022"),
            entry(DATE_FO_ELIGIBLE_FROM, "23 July 2022"),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(DocmosisTemplateConstants.CASE_REFERENCE, "1616-5914-0147-3378"),
            entry(DATE_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
            entry(TIME_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER)),
            entry(CommonContent.NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME)
        );
    }

}
