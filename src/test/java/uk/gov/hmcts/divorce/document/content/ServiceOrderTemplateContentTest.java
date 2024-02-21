package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ORDER_TYPE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SEPARATION_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SEPARATION_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(MockitoExtension.class)
public class ServiceOrderTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ServiceOrderTemplateContent serviceOrderTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDispensedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DISPENSED);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put("dueDateString", ". You can apply from 20 June 2021.");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingJudicialSeparationDispensedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DISPENSED);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, JUDICIAL_SEPARATION_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, SEPARATION_ORDER);
        expectedEntries.put("dueDateString", ".");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingSeparationDispensedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DISPENSED);
        caseData.setSupplementaryCaseType(SEPARATION);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, SEPARATION_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, SEPARATION_ORDER);
        expectedEntries.put("dueDateString", ".");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDeemedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DEEMED);
        caseData.getAlternativeService().setDeemedServiceDate(LocalDate.of(2021, 6, 20));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "20 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put("dueDateString", " on 20 June 2021.");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingJudicialSeparationDeemedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DEEMED);
        caseData.getAlternativeService().setDeemedServiceDate(LocalDate.of(2021, 6, 20));
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "20 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, JUDICIAL_SEPARATION_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, SEPARATION_ORDER);
        expectedEntries.put("dueDateString", ".");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingSeparationDeemedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DEEMED);
        caseData.getAlternativeService().setDeemedServiceDate(LocalDate.of(2021, 6, 20));
        caseData.setSupplementaryCaseType(SEPARATION);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_DECISION_DATE, "20 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, SEPARATION_PROCESS);
        expectedEntries.put(DUE_DATE, "20 June 2021");
        expectedEntries.put(ORDER_TYPE, SEPARATION_ORDER);
        expectedEntries.put("dueDateString", ".");
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("spouse");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
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
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("civil partner");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
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
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("spouse");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "spouse");
        expectedEntries.put(IS_DIVORCE, YES);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS);
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("civil partner");

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, FORMATTED_TEST_CASE_ID);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, "18 June 2021");
        expectedEntries.put(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021");
        expectedEntries.put(PETITIONER_FULL_NAME, "pet full test_middle_name name");
        expectedEntries.put(RESPONDENT_FULL_NAME, "resp full name");
        expectedEntries.put(IS_SERVICE_ORDER_TYPE_DEEMED, YES);
        expectedEntries.put(REFUSAL_REASON, "refusal reasons");
        expectedEntries.put(PARTNER, "civil partner");
        expectedEntries.put(IS_DIVORCE, NO);
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(ORDER_TYPE, CONDITIONAL_ORDER);
        expectedEntries.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        expectedEntries.put(ISSUE_DATE_POPULATED, true);

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);
        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldApplyWelshContentIfApplicant1LanguagePreferenceIsWelshOnDivorce() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), WELSH)).thenReturn("priod");
        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(PARTNER, "priod"),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE_PROCESS_CY),
            entry(CTSC_CONTACT_DETAILS, buildCtscContactDetails())
        );
    }

    @Test
    public void shouldApplyWelshContentIfApplicant1LanguagePreferenceIsWelshOnDissolution() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), WELSH)).thenReturn("partner sifil");
        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(PARTNER, "partner sifil"),
            entry(DIVORCE_OR_DISSOLUTION, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY),
            entry(CTSC_CONTACT_DETAILS, buildCtscContactDetails())
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
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 20));
        return caseData;
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .build();
    }
}
