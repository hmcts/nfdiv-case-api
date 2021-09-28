package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JOINT_OR_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LINE_1_LINE_2_CITY_POSTCODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
public class DivorceApplicationSoleTemplateContentTest {

    @InjectMocks
    private DivorceApplicationSoleTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationWithTypeDivorce() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);

        setCaseDetails(caseData);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(HAS_OTHER_COURT_CASES, true),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, CONDITIONAL_ORDER_OF_DIVORCE_FROM),
            entry(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(MARRIAGE_DATE, null),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(FINANCIAL_ORDER_CHILD_SOLE, AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(JOINT_OR_SOLE, DocmosisTemplateConstants.SOLE_APPLICATION),
            entry(IS_SOLE, true),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE),
            entry(HAS_FINANCIAL_ORDERS, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(FINANCIAL_ORDER_CHILD_JOINT, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationWithTypeDissolution() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        setCaseDetails(caseData);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(HAS_OTHER_COURT_CASES, true),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH),
            entry(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(MARRIAGE_DATE, null),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(FINANCIAL_ORDER_CHILD_SOLE, AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(JOINT_OR_SOLE, DocmosisTemplateConstants.SOLE_APPLICATION),
            entry(IS_SOLE, true),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP),
            entry(HAS_FINANCIAL_ORDERS, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(FINANCIAL_ORDER_CHILD_JOINT, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForJointApplicationWithTypeDivorce() {
        CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);

        setCaseDetails(caseData);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(HAS_OTHER_COURT_CASES, true),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, CONDITIONAL_ORDER_OF_DIVORCE),
            entry(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(MARRIAGE_DATE, null),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(FINANCIAL_ORDER_CHILD_SOLE, AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(JOINT_OR_SOLE, JOINT_APPLICATION),
            entry(IS_SOLE, false),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE),
            entry(HAS_FINANCIAL_ORDERS, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(FINANCIAL_ORDER_CHILD_JOINT, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2));
    }

    @Test
    public void shouldSuccessfullyApplyApplicant2PostalAddressIfApplicant2IsSolicitorRepresented() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setLegalProceedings(YES);

        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .address("223b\nBaker Street\nLondon\nGreater London\nNW1 5FG\nUnited Kingdom")
                .build()
        );

        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("221b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();

        caseData.getApplicant1().setHomeAddress(address);
        caseData.getApplicant1().setFinancialOrder(NO);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_2_POSTAL_ADDRESS, "223b\nBaker Street\nLondon\nGreater London\nNW1 5FG\nUnited Kingdom")
        );
    }

    @Test
    public void shouldSuccessfullyApplyApplicant2PostalAddressIfApplicant2IsNotSolicitorRepresentedAndIsSolicitorApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setLegalProceedings(YES);

        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setSolicitorRepresented(YES);

        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("221b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();

        caseData.getApplicant2().setCorrespondenceAddress(address);

        caseData.getApplicant1().setHomeAddress(address);
        caseData.getApplicant1().setFinancialOrder(NO);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_2_POSTAL_ADDRESS, "221b\nBaker Street\nLondon\nGreater London\nNW1 6XE\nUnited Kingdom")
        );
    }

    @Test
    public void shouldSuccessfullyApplyApplicant2PostalAddressIfApplicant2IsNotSolicitorRepresentedAndIsCitizenApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setLegalProceedings(YES);

        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setSolicitorRepresented(YES);

        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("221b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();

        caseData.getApplicant2().setHomeAddress(address);

        caseData.getApplicant1().setHomeAddress(address);
        caseData.getApplicant1().setFinancialOrder(NO);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(APPLICANT_2_POSTAL_ADDRESS, "221b\nBaker Street\nLondon\nGreater London\nNW1 6XE\nUnited Kingdom")
        );
    }

    private void setCaseDetails(CaseData caseData) {
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant1().setLegalProceedings(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );
    }
}
