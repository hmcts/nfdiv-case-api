package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderOfflineClarificationContent.REASON_JURISDICTION_DETAILS;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderOfflineClarificationContent.REASON_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderOfflineClarificationContent.REASON_MARRIAGE_CERT_TRANSLATION;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderOfflineClarificationContent.REASON_PREVIOUS_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConditionalOrderOfflineClarificationContentIT {

    @Autowired
    private ConditionalOrderOfflineClarificationContent conditionalOrderOfflineClarificationContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForOfflineClarificationConditionalOrderDocument() {

        CaseData caseData = caseData();
        caseData.setIsJudicialSeparation(NO);

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(new HashSet<>())
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

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        expectedEntries.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put("isSole", caseData.getApplicationType().isSole());
        expectedEntries.put("isJoint", !caseData.getApplicationType().isSole());
        expectedEntries.put("judicialSeparation", caseData.getIsJudicialSeparation().toBoolean());
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);

        final Set<ClarificationReason> clarificationReasons = caseData.getConditionalOrder().getRefusalClarificationReason();

        expectedEntries.put(REASON_JURISDICTION_DETAILS,
            clarificationReasons.contains(ClarificationReason.JURISDICTION_DETAILS));
        expectedEntries.put(REASON_MARRIAGE_CERT_TRANSLATION,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE_TRANSLATION));
        expectedEntries.put(REASON_MARRIAGE_CERTIFICATE,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE));
        expectedEntries.put(REASON_PREVIOUS_PROCEEDINGS_DETAILS,
            clarificationReasons.contains(ClarificationReason.PREVIOUS_PROCEEDINGS_DETAILS));

        expectedEntries.put("legalAdvisorComments", emptyList());

        expectedEntries.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        Map<String, Object> templateContent = conditionalOrderOfflineClarificationContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
