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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
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

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "No"),
            entry("ctscContactDetails", buildCtscContactDetails())
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDeemedWithServiceGrantedDocument() {
        CaseData caseData = buildCaseData(YES, DEEMED);
        caseData.getAlternativeService().setDeemedServiceDate(LocalDate.of(2021, 6, 20));

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(SERVICE_APPLICATION_DECISION_DATE, "20 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "Yes"),
            entry("ctscContactDetails", buildCtscContactDetails())
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "No"),
            entry(REFUSAL_REASON, "refusal reasons"),
            entry(PARTNER, "wife"),
            entry(IS_DIVORCE, "Yes"),
            entry("ctscContactDetails", ctscContactDetails)
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDispensedWithServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DISPENSED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "No"),
            entry(REFUSAL_REASON, "refusal reasons"),
            entry(PARTNER, "civil partner"),
            entry(IS_DIVORCE, "No"),
            entry("ctscContactDetails", ctscContactDetails)
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDivorceCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "Yes"),
            entry(REFUSAL_REASON, "refusal reasons"),
            entry(PARTNER, "wife"),
            entry(IS_DIVORCE, "Yes"),
            entry("ctscContactDetails", ctscContactDetails)
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromDissolutionCaseDataForGeneratingDeemedServiceRefusalDocument() {
        CaseData caseData = buildCaseData(NO, DEEMED);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reasons");

        Map<String, Object> templateContent = serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = buildCtscContactDetails();
        ctscContactDetails.setEmailAddress("divorcecase@justice.gov.uk");

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_SERVICE_ORDER_TYPE_DEEMED, "Yes"),
            entry(REFUSAL_REASON, "refusal reasons"),
            entry(PARTNER, "civil partner"),
            entry(IS_DIVORCE, "No"),
            entry("ctscContactDetails", ctscContactDetails)
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
        return caseData;
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("divorcecase@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 12706")
            .town("Harlow")
            .postcode("CM20 9QT")
            .phoneNumber("0300 303 0642")
            .build();
    }
}
