package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.ASSISTANT_JUSTICES_CLERK;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.PROPER_OFFICER_OF_THE_COURT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_MADE_BY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_HEADING;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
public class GeneralOrderTemplateContentIT {

    @Autowired
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Test
    public void shouldSuccessfullyApplySoleContentFromCaseDataForGeneratingGeneralOrderDocument() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("divorcecase@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .build();

        assertThat(templateContent).contains(
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(GENERAL_ORDER_DATE, "1 January 2021"),
            entry(GENERAL_ORDER_DETAILS, "some details"),
            entry(GENERAL_ORDER_MADE_BY, "judge some name"),
            entry("sitting", ", sitting"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(APPLICANT_HEADING, "Applicant"),
            entry(RESPONDENT_HEADING, "Respondent"),
            entry(CTSC_CONTACT_DETAILS, ctscContactDetails)
        );
    }

    @Test
    public void shouldSuccessfullyApplyJointContentFromCaseDataForGeneratingGeneralOrderDocument() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("divorcecase@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .build();

        assertThat(templateContent).contains(
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(GENERAL_ORDER_DATE, "1 January 2021"),
            entry(GENERAL_ORDER_DETAILS, "some details"),
            entry(GENERAL_ORDER_MADE_BY, "judge some name"),
            entry("sitting", ", sitting"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(APPLICANT_HEADING, "Applicant 1"),
            entry(RESPONDENT_HEADING, "Applicant 2"),
            entry(CTSC_CONTACT_DETAILS, ctscContactDetails)
        );
    }

    @Test
    public void shouldApplyAssistantJusticeClerkContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(ASSISTANT_JUSTICES_CLERK);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "an assistant justices clerk")
        );
    }

    @Test
    public void shouldApplyProperOfficerOfTheCourtContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(PROPER_OFFICER_OF_THE_COURT);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "a proper officer of the court")
        );
    }
}
