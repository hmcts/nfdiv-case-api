package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.*;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConditionalOrderRefusalContentIT {

    @Autowired
    private ConditionalOrderRefusalContent conditionalOrderRefusalContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForRefusalConditionalOrderDocumentForDivorceApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .refusalRejectionAdditionalInfo("Rejected comments")
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, 1616591401473378L);
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_2_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE_POPULATED, true);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put("hasLegalAdvisorComments", true);
        expectedEntries.put("legalAdvisorComments", caseData.getConditionalOrder().getRefusalRejectionAdditionalInfo());
        expectedEntries.put("phoneNumber", "0300 303 0642");

        Map<String, Object> templateContent = conditionalOrderRefusalContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForRefusalConditionalOrderDocumentForCivilPartnershipApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .refusalRejectionAdditionalInfo("Rejected comments")
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, 1616591401473378L);
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_2_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE_POPULATED, true);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        expectedEntries.put("hasLegalAdvisorComments", true);
        expectedEntries.put("legalAdvisorComments", caseData.getConditionalOrder().getRefusalRejectionAdditionalInfo());
        expectedEntries.put("phoneNumber", "0300 303 0642");

        Map<String, Object> templateContent = conditionalOrderRefusalContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
