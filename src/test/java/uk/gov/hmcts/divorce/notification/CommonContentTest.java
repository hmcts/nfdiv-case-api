package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    private final Long caseRef = 7201000100010001L;
    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVarsForDivorceNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "divorce.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldSetCommonTemplateVarsForDissolutionNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setDivorceOrDissolution(DISSOLUTION);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DISSOLUTION_COURT_EMAIL, "dissolution.court@email.com"));

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "dissolution.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldGetPartner() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("wife");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("husband");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("spouse");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("civil partner");
    }

    @Test
    void shouldGetPartnerWelshContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gwraig");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gŵr");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("priod");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("partner sifil");
    }

    @Test
    void shouldGetUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetEnglishUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData, ENGLISH)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData, ENGLISH)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetWelshUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("ysgariad");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("diddymiad");
    }

    @Test
    void shouldSetTemplateVarsForSoleApplication() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, TEST_CASE_ID, getApplicant(), respondent());

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "no"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsMale() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, TEST_CASE_ID, getApplicant(FEMALE), getApplicant(MALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "yes"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldNotThrowNpeIfGenderIsNull() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        assertDoesNotThrow(() -> commonContent
            .conditionalOrderTemplateVars(caseData, TEST_CASE_ID, getApplicant(null), getApplicant(null))
        );
    }


    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsFemale() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, TEST_CASE_ID, getApplicant(MALE), getApplicant(FEMALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "yes"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDissolution() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, TEST_CASE_ID, getApplicant(MALE), getApplicant(FEMALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "yes")
            );
    }

    @Test
    void shouldReturnProfessionalSignInUrl() {
        Long caseId = 123456789L;
        when(emailTemplatesConfig.getTemplateVars())
            .thenReturn(Map.of(SIGN_IN_PROFESSIONAL_USERS_URL, "http://professional-sing-in-url/"));

        String professionalSignInUrl = commonContent.getProfessionalUsersSignInUrl(caseId);

        assertThat(professionalSignInUrl).isEqualTo("http://professional-sing-in-url/123456789");
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant1PrefersWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(YES)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, TEST_CASE_ID, applicant1, applicant2);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gwraig")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant1DoesNotPreferWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, TEST_CASE_ID, applicant1, applicant2);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "wife")
            );
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant2PrefersWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .languagePreferenceWelsh(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, TEST_CASE_ID, applicant2, applicant1);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gŵr")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant2DoesNotPreferWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, TEST_CASE_ID, applicant2, applicant1);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "husband")
            );
    }

    @Test
    void shouldAddCoRefusedSolicitorContentForSoleApplicationWithRefusalOptionMoreInfo() {

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 6, 22))
                .build())
            .applicationType(SOLE_APPLICATION)
            .applicant1(applicantRepresentedBySolicitor())
            .applicant2(respondent())
            .build();

        final Map<String, String> result = commonContent.getCoRefusedSolicitorTemplateVars(caseData, TEST_CASE_ID,
            caseData.getApplicant1(), MORE_INFO);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry("isJoint", CommonContent.NO),
                entry("moreInfo", CommonContent.YES),
                entry("amendApplication", CommonContent.NO),
                entry(SOLICITOR_NAME, "The Solicitor"),
                entry(SOLICITOR_REFERENCE, "Not provided"),
                entry("applicant1Label", "Applicant"),
                entry("applicant2Label", "Respondent"),
                entry(ISSUE_DATE, "22 June 2022"),
                entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
                entry(APPLICANT_2_FULL_NAME, "applicant_2_first_name test_last_name")
            );
    }

    @Test
    void shouldAddCoRefusedSolicitorContentForJointApplicationWithRefusalOptionAmendApplication() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 6, 22))
                .build())
            .applicationType(JOINT_APPLICATION)
            .applicant1(getApplicant())
            .applicant2(applicantRepresentedBySolicitor())
            .build();

        caseData.getApplicant2().getSolicitor().setReference("sol2");

        final Map<String, String> result = commonContent.getCoRefusedSolicitorTemplateVars(caseData, TEST_CASE_ID,
            caseData.getApplicant2(), REJECT);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry("isJoint", CommonContent.YES),
                entry("moreInfo", CommonContent.NO),
                entry("amendApplication", CommonContent.YES),
                entry(SOLICITOR_NAME, "The Solicitor"),
                entry(SOLICITOR_REFERENCE, "sol2"),
                entry("applicant1Label", "Applicant 1"),
                entry("applicant2Label", "Applicant 2"),
                entry(ISSUE_DATE, "22 June 2022"),
                entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
                entry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name")
            );
    }

    @ParameterizedTest
    @CsvSource({"YES,yes, no", "NO,no,yes", ",no,yes"})
    void shouldSetOverdueAndInTimeVariablesFinalOrderOverdue(YesOrNo finalOrderOverdue, String isOverdue, String inTime) {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().isFinalOrderOverdue(finalOrderOverdue).build())
            .build();

        final HashMap<String, String> templateVars = new HashMap<>();

        commonContent.setOverdueAndInTimeVariables(caseData, templateVars);

        assertThat(templateVars.get(IS_OVERDUE)).isEqualTo(isOverdue);
        assertThat(templateVars.get(IN_TIME)).isEqualTo(inTime);
    }

    @Test
    void shouldReturnContentWhenAbleToApplyForCoOrFo() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 6, 22))
                .build())
            .applicationType(JOINT_APPLICATION)
            .applicant1(getApplicantWithAddress())
            .applicant2(getApplicant(MALE))
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(any())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final LocalDate localDate = LocalDate.now();

        final Map<String, Object> result = commonContent.templateContentCanApplyForCoOrFo(caseData, TEST_CASE_ID,
            caseData.getApplicant1(), caseData.getApplicant2(), localDate);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry("firstName", TEST_FIRST_NAME),
                entry("lastName", TEST_LAST_NAME),
                entry(ADDRESS, "line 1\ntown\nUK\npostcode"),
                entry(PARTNER, "husband"),
                entry(DATE, localDate),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, true)
            );
    }

    @Test
    void shouldReturnContentWhenAbleToApplyForCoOrFoWelsh() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 6, 22))
                .build())
            .applicationType(SOLE_APPLICATION)
            .applicant1(getApplicantWithAddress())
            .applicant2(getApplicant(MALE))
            .build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(any())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final LocalDate localDate = LocalDate.now();

        final Map<String, Object> result = commonContent.templateContentCanApplyForCoOrFo(caseData, TEST_CASE_ID,
            caseData.getApplicant1(), caseData.getApplicant2(), localDate);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry("firstName", TEST_FIRST_NAME),
                entry("lastName", TEST_LAST_NAME),
                entry(ADDRESS, "line 1\ntown\nUK\npostcode"),
                entry(PARTNER, "partner sifil"),
                entry(DATE, localDate),
                entry(IS_JOINT, false),
                entry(IS_DIVORCE, false)
            );
    }

    @Test
    void shouldSetDivorceNotDissolution() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        Map<String, String> templateContent = new HashMap<>();

        commonContent.setIsDivorceAndIsDissolutionVariables(caseData, templateContent);

        assertThat(templateContent)
            .isNotEmpty()
            .contains(
                entry(IS_DIVORCE, "yes"),
                entry(IS_DISSOLUTION, "no")
            );
    }

    @Test
    void shouldSetDissolutionNotDivorce() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();

        Map<String, String> templateContent = new HashMap<>();

        commonContent.setIsDivorceAndIsDissolutionVariables(caseData, templateContent);

        assertThat(templateContent)
            .isNotEmpty()
            .contains(
                entry(IS_DIVORCE, "no"),
                entry(IS_DISSOLUTION, "yes")
            );
    }

    @Test
    void nocCitizenTemplateVars() {
        Applicant applicant = new Applicant();
        applicant.setFirstName("John");
        applicant.setLastName("Doe");

        Solicitor solicitor = new Solicitor();
        solicitor.setFirmName("XYZ Solicitors");
        applicant.setSolicitor(solicitor);

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SMART_SURVEY, "https://testsurveylink"));
        Map<String, String> templateVars = commonContent.nocCitizenTemplateVars(caseRef, applicant);

        // Assert the result
        assertNotNull(templateVars);
        assertEquals("John", templateVars.get("first name"));
        assertEquals("Doe", templateVars.get("last name"));
        assertEquals("XYZ Solicitors", templateVars.get("solicitor firm"));
        assertEquals("https://testsurveylink", templateVars.get(CommonContent.SMART_SURVEY));
        assertEquals("7201-0001-0001-0001", templateVars.get(CommonContent.APPLICATION_REFERENCE));

    }

    @Test
    void testNocSolsTemplateVars() {
        Solicitor solicitor = Solicitor.builder()
            .name("Solicitor Name")
            .reference("SolRef")
            .build();
        Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .build();
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SMART_SURVEY, "https://testsurveylink"));
        Map<String, String> templateVars = commonContent.nocSolsTemplateVars(caseRef, applicant);

        assertEquals("7201-0001-0001-0001", templateVars.get(CommonContent.APPLICATION_REFERENCE));
        assertEquals("Solicitor Name", templateVars.get(CommonContent.NAME));
        assertEquals("SolRef", templateVars.get(CommonContent.SOLICITOR_REFERENCE));
        assertEquals("https://testsurveylink", templateVars.get(CommonContent.SMART_SURVEY));
    }

    @Test
    void testNocSolsTemplateVarsNoSolsReference() {
        Solicitor solicitor = Solicitor.builder()
            .name("Solicitor Name")
            .build();
        Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .build();
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SMART_SURVEY, "https://testsurveylink"));
        Map<String, String> templateVars = commonContent.nocSolsTemplateVars(caseRef, applicant);

        assertEquals("7201-0001-0001-0001", templateVars.get(CommonContent.APPLICATION_REFERENCE));
        assertEquals("Solicitor Name", templateVars.get(CommonContent.NAME));
        assertEquals("Not provided", templateVars.get(SOLICITOR_REFERENCE));
        assertEquals("https://testsurveylink", templateVars.get(CommonContent.SMART_SURVEY));
    }

    @Test
    void testNocOldSolsTemplateVars() {
        Solicitor solicitor = Solicitor.builder()
            .name("Old Solicitor Name")
            .build();
        Applicant beforeApplicant = Applicant.builder()
            .solicitor(solicitor)
            .firstName("First")
            .lastName("Last")
            .build();

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SMART_SURVEY, "https://testsurveylink"));

        Map<String, String> templateVars = commonContent.nocOldSolsTemplateVars(caseRef, beforeApplicant);
        assertEquals("7201-0001-0001-0001", templateVars.get(CommonContent.APPLICATION_REFERENCE));
        assertEquals("Old Solicitor Name", templateVars.get(CommonContent.NAME));
        assertEquals("First Last", templateVars.get(CommonContent.APPLICANT_NAME));
        assertEquals("https://testsurveylink", templateVars.get(CommonContent.SMART_SURVEY));
    }
}

