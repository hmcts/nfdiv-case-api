package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class ConditionalOrderReminderTemplateContentTest {

    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrEndCivilPartnershipApplication";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP = "divorceOrEndCivilPartnership";
    public static final String APPLICANT_FIRST_NAME = "applicantFirstName";
    public static final String APPLICANT_LAST_NAME = "applicantLastName";
    public static final String APPLICANT_ADDRESS = "applicantAddress";

    public static final String DIVORCE = "get a divorce";
    public static final String END_THE_CIVIL_PARTNERSHIP = "end the civil partnership";

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderReminderTemplateContent conditionalOrderReminderTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForConditionalOrderReminderDocumentDivorceSole() {

        CaseData caseData = caseData();

        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(applicantAddress());
        caseData.getApplicant2().setGender(FEMALE);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(APPLICANT_FIRST_NAME, caseData.getApplicant1().getFirstName());
        expectedEntries.put(APPLICANT_LAST_NAME, caseData.getApplicant1().getLastName());
        expectedEntries.put(APPLICANT_ADDRESS, caseData.getApplicant1().getAddress());
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(IS_JOINT, !caseData.getApplicationType().isSole());

        Map<String, Object> templateContent = conditionalOrderReminderTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForConditionalOrderReminderDocumentCivilPartnershipJoint() {

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(applicantAddress());
        caseData.getApplicant2().setGender(MALE);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(APPLICANT_FIRST_NAME, caseData.getApplicant1().getFirstName());
        expectedEntries.put(APPLICANT_LAST_NAME, caseData.getApplicant1().getLastName());
        expectedEntries.put(APPLICANT_ADDRESS, caseData.getApplicant1().getAddress());
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, TO_END_A_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, END_THE_CIVIL_PARTNERSHIP);
        expectedEntries.put(IS_JOINT, !caseData.getApplicationType().isSole());

        Map<String, Object> templateContent = conditionalOrderReminderTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldReturnSupportedTemplates() {
        assertThat(conditionalOrderReminderTemplateContent.getSupportedTemplates())
            .isEqualTo(List.of(CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID));
    }

    private AddressGlobalUK applicantAddress() {
        return AddressGlobalUK.builder()
            .addressLine1("223b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();
    }
}
