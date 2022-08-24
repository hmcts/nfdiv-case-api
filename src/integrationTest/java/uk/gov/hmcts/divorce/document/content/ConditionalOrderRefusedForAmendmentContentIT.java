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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConditionalOrderRefusedForAmendmentContentIT {

    @Autowired
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForClarificationConditionalOrderDocument() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("legalAdvisorComments", emptyList());
        expectedEntries.put(PARTNER, "spouse");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

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

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Rejected comments")));
        expectedEntries.put(PARTNER, "wife");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForRejectRefusalOrderDocumentForCivilPartnershipApplication() {

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
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Rejected comments")));
        expectedEntries.put(PARTNER, "civil partner");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForClarificationRefusalOrderDocumentForCivilPartnershipApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
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
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason(MARRIAGE_CERTIFICATE.getLabel()),
                new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Clarification comments")));
        expectedEntries.put(PARTNER, "civil partner");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentInWelshFromCaseDataForRefusalConditionalOrderDocumentForDivorceApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .refusalRejectionAdditionalInfo("Rejected comments")
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE_WELSH);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Rejected comments")));
        expectedEntries.put(PARTNER, "gwraig");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentInWelshFromCaseDataForRefusalConditionalOrderDocumentForOfflineDivorceApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .refusalRejectionAdditionalInfo("Rejected comments")
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE_WELSH);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Rejected comments")));
        expectedEntries.put(PARTNER, "priod");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentInWelshFromCaseDataForClarificationRefusalOrderDocumentForCivilPartnershipApplication() {

        CaseData caseData = caseData();

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build();
        caseData.setConditionalOrder(conditionalOrder);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY);
        expectedEntries.put("legalAdvisorComments",
            List.of(new ConditionalOrderRefusedForAmendmentContent.RefusalReason(MARRIAGE_CERTIFICATE.getLabel()),
                new ConditionalOrderRefusedForAmendmentContent.RefusalReason("Clarification comments")));
        expectedEntries.put(PARTNER, "partner sifil");

        Map<String, Object> templateContent = conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
