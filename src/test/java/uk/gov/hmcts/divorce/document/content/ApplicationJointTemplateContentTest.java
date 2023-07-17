package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MARRIAGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MARRIAGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class ApplicationJointTemplateContentTest {

    @Mock
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Mock
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    @InjectMocks
    private ApplicationJointTemplateContent applicationJointTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForJointApplicationWithTypeDivorce() {

        CaseData caseData = buildCaseData(DIVORCE, NO, NO);

        final Map<String, Object> result = applicationJointTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce."),
            entry(DIVORCE_OR_DISSOLUTION, "divorce application"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE_POPULATED, true),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_2_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_2, false),
            entry(APPLICANT_2_COURT_CASE_DETAILS, null),
            entry(APPLICANT_1_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name()),
            entry(APPLICANT_2_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name()),
            entry(IS_DIVORCE, true)
        );

        verify(applicantTemplateDataProvider, times(2)).deriveJointFinancialOrder(any(Applicant.class), eq(false));
        verify(applicantTemplateDataProvider).mapContactDetails(any(Applicant.class), any(Applicant.class), anyMap());
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID), eq(ENGLISH));
        verify(applicationTemplateDataProvider).mapMarriageDetails(anyMap(), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForJointApplicationWithTypeDissolution() {

        CaseData caseData = buildCaseData(DISSOLUTION, NO, NO);

        final Map<String, Object> result = applicationJointTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of their civil partnership."),
            entry(DIVORCE_OR_DISSOLUTION, "application to end a civil partnership"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE_POPULATED, true),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_2_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_2, false),
            entry(APPLICANT_2_COURT_CASE_DETAILS, null)
        );

        verify(applicantTemplateDataProvider, times(2))
            .deriveJointFinancialOrder(any(Applicant.class), eq(false));
        verify(applicantTemplateDataProvider).mapContactDetails(any(Applicant.class), any(Applicant.class), anyMap());
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID), eq(ENGLISH));
        verify(applicationTemplateDataProvider).mapMarriageDetails(anyMap(), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentInWelshFromCaseDataForJointApplicationWithTypeDivorce() {

        CaseData caseData = buildCaseData(DIVORCE, YES, YES);

        final Map<String, Object> result = applicationJointTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "am orchymyn ysgaru terfynol."),
            entry(DIVORCE_OR_DISSOLUTION, "am ysgariad"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE_CY),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "yr ysgariad"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE_POPULATED, true),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_2_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_2, false),
            entry(APPLICANT_2_COURT_CASE_DETAILS, null),
            entry(APPLICANT_1_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name()),
            entry(APPLICANT_2_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name())
        );

        verify(applicantTemplateDataProvider, times(2)).deriveJointFinancialOrder(any(Applicant.class), eq(true));
        verify(applicantTemplateDataProvider).mapContactDetails(any(Applicant.class), any(Applicant.class), anyMap());
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID), eq(WELSH));
        verify(applicationTemplateDataProvider).mapMarriageDetails(anyMap(), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentInWelshFromCaseDataForJointApplicationWithTypeDissolution() {

        CaseData caseData = buildCaseData(DISSOLUTION, YES, YES);

        final Map<String, Object> result = applicationJointTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "i ddiddymu eu partneriaeth sifil."),
            entry(DIVORCE_OR_DISSOLUTION, "i ddod â phartneriaeth sifil i ben"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP_CY),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ddod â’r bartneriaeth sifil i ben"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE_POPULATED, true),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_2_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_2, false),
            entry(APPLICANT_2_COURT_CASE_DETAILS, null),
            entry(APPLICANT_1_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name()),
            entry(APPLICANT_2_MARRIAGE_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name())
        );

        verify(applicantTemplateDataProvider, times(2)).deriveJointFinancialOrder(any(Applicant.class), eq(true));
        verify(applicantTemplateDataProvider).mapContactDetails(any(Applicant.class), any(Applicant.class), anyMap());
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID), eq(WELSH));
        verify(applicationTemplateDataProvider).mapMarriageDetails(anyMap(), any(Application.class));
    }

    private CaseData buildCaseData(final DivorceOrDissolution divorceOrDissolution, final YesOrNo app1LangPrefWelsh,
        final YesOrNo app2LangPrefWelsh) {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .financialOrder(NO)
            .legalProceedings(NO)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .languagePreferenceWelsh(app1LangPrefWelsh)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_APP2_FIRST_NAME)
            .middleName(TEST_APP2_MIDDLE_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .financialOrder(NO)
            .legalProceedings(NO)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .languagePreferenceWelsh(app2LangPrefWelsh)
            .build();

        final String applicant1MarriageName = TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME;
        final String applicant2MarriageName = TEST_APP2_FIRST_NAME + " " + TEST_APP2_MIDDLE_NAME + " " + TEST_APP2_LAST_NAME;

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(divorceOrDissolution)
            .application(Application.builder()
                .issueDate(LocalDate.of(2021, 4, 28))
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        caseData.getApplication().getMarriageDetails().setApplicant1Name(applicant1MarriageName);
        caseData.getApplication().getMarriageDetails().setApplicant2Name(applicant2MarriageName);

        return caseData;
    }
}
