package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ServiceOrderTemplateContentIT {

    @Autowired
    private ServiceOrderTemplateContent serviceOrderTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDispensedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DISPENSED);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put("ctscContactDetails", buildCtscContactDetails());

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDeemedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DEEMED);
        caseData.getAlternativeService().setDeemedServiceDate(LocalDate.of(2021, 6, 20));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "20 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put("ctscContactDetails", buildCtscContactDetails());

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "spouse");
        expectedEntries.put(IS_DIVORCE, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "civil partner");
        expectedEntries.put(IS_DIVORCE, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "spouse");
        expectedEntries.put(IS_DIVORCE, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, "1616-5914-0147-3378");
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "civil partner");
        expectedEntries.put(IS_DIVORCE, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldApplyWelshContentIfApplicant1LanguagePreferenceIsWelshOnDivorce() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(PARTNER, "priod"),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS_CY)
        );
    }

    @Test
    public void shouldApplyWelshContentIfApplicant1LanguagePreferenceIsWelshOnDissolution() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(PARTNER, "partner sifil"),
            entry(DIVORCE_OR_DISSOLUTION, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY)
        );
    }

    private CaseData buildCaseData(final YesOrNo serviceApplicationGranted,
                                   final AlternativeServiceType serviceType) {

        CaseData caseData = caseData();
        caseData.setAlternativeService(
            AlternativeService
                .builder()
                .serviceApplicationGranted(serviceApplicationGranted)
                .alternativeServiceType(serviceType)
                .serviceApplicationDecisionDate(LocalDate.of(2021, 6, 18))
                .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                .build()
        );
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        return caseData;
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .emailAddress("divorcecase@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .build();
    }
}
