package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant2Represented;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class DivorceApplicationSoleTemplateContentTest {

    @Mock
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Mock
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    @InjectMocks
    private DivorceApplicationSoleTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationWithTypeDivorce() {

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .financialOrder(NO)
            .legalProceedings(NO)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .application(Application.builder()
                .issueDate(LocalDate.of(2021, 4, 28))
                .applicant1IsApplicant2Represented(Applicant2Represented.NOT_SURE)
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        caseData.getApplication().getMarriageDetails().setApplicant1Name(TEST_LAST_NAME);
        caseData.getApplication().getMarriageDetails().setApplicant2Name(TEST_LAST_NAME);

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from"),
            entry(DIVORCE_OR_DISSOLUTION, "divorce application"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_FULL_NAME, applicant1.getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_2_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_2_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, applicant2.getFullName()),
            entry(APPLICANT_2_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_EMAIL, TEST_USER_EMAIL),
            entry(PLACE_OF_MARRIAGE, null),
            entry(MARRIAGE_DATE, null)
        );

        verify(applicantTemplateDataProvider).deriveSoleFinancialOrder(any(Applicant.class));
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationWithTypeDissolution() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .financialOrder(NO)
            .legalProceedings(NO)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .solicitor(Solicitor.builder()
                .name(TEST_SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .firmName(TEST_SOLICITOR_NAME)
                .address(TEST_SOLICITOR_ADDRESS)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .application(Application.builder()
                .issueDate(LocalDate.of(2021, 4, 28))
                .applicant1IsApplicant2Represented(Applicant2Represented.YES)
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        caseData.getApplication().getMarriageDetails().setApplicant1Name(TEST_LAST_NAME);
        caseData.getApplication().getMarriageDetails().setApplicant2Name(TEST_LAST_NAME);

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH),
            entry(DIVORCE_OR_DISSOLUTION, "application to end your civil partnership"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_FULL_NAME, applicant1.getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_2_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_2_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, applicant2.getFullName()),
            entry(APPLICANT_2_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_EMAIL, TEST_USER_EMAIL),
            entry(PLACE_OF_MARRIAGE, null),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS, true),
            entry(APPLICANT_2_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(APPLICANT_2_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL),
            entry(APPLICANT_2_SOLICITOR_FIRM_NAME, TEST_SOLICITOR_NAME),
            entry(APPLICANT_2_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS)
        );

        verify(applicantTemplateDataProvider).deriveSoleFinancialOrder(any(Applicant.class));
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationWithFormattedSolicitorAddress() {
        final String solAddressWithNewLine = "10 Solicitor Road\ntown\n\npostcode\n";
        final String solAddressWithCleanUp = "10 Solicitor Road\ntown\npostcode";

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .financialOrder(NO)
            .legalProceedings(NO)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .contactDetailsType(PUBLIC)
            .solicitor(Solicitor.builder()
                .address(solAddressWithNewLine)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .application(Application.builder()
                .issueDate(LocalDate.of(2021, 4, 28))
                .applicant1IsApplicant2Represented(Applicant2Represented.YES)
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        caseData.getApplication().getMarriageDetails().setApplicant1Name(TEST_LAST_NAME);
        caseData.getApplication().getMarriageDetails().setApplicant2Name(TEST_LAST_NAME);

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from"),
            entry(DIVORCE_OR_DISSOLUTION, "divorce application"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_FULL_NAME, applicant1.getFullName()),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_1_EMAIL, TEST_USER_EMAIL),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(APPLICANT_1_FINANCIAL_ORDER, null),
            entry(HAS_OTHER_COURT_CASES_APPLICANT_1, false),
            entry(APPLICANT_1_COURT_CASE_DETAILS, null),
            entry(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_2_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_2_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, applicant2.getFullName()),
            entry(APPLICANT_2_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_EMAIL, TEST_USER_EMAIL),
            entry(PLACE_OF_MARRIAGE, null),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS, true),
            entry(APPLICANT_2_SOLICITOR_ADDRESS, solAddressWithCleanUp)
        );

        verify(applicantTemplateDataProvider).deriveSoleFinancialOrder(any(Applicant.class));
        verify(applicationTemplateDataProvider).deriveJurisdictionList(any(Application.class), eq(TEST_CASE_ID));
    }
}
