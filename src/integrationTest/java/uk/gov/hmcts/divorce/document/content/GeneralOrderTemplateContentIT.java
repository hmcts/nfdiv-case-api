package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.ASSISTANT_JUSTICES_CLERK;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.PROPER_OFFICER_OF_THE_COURT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_MADE_BY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SITTING_VENUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;


@ExtendWith(SpringExtension.class)
@SpringBootTest
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
            .emailAddress("contactdivorce@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress("contactdivorce@justice.gov.uk")
            .build();

        assertThat(templateContent).contains(
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(GENERAL_ORDER_DATE, "1 January 2021"),
            entry(GENERAL_ORDER_DETAILS, "some details"),
            entry(GENERAL_ORDER_MADE_BY, "District Judge some name"),
            entry(SITTING_VENUE, "Petty France, London"),
            entry("sitting", ", sitting"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(APPLICANT_HEADING, "Applicant"),
            entry(RESPONDENT_HEADING, "Respondent"),
            entry(GENERAL_ORDER_RECITALS, "test recitals"),
            entry(CTSC_CONTACT_DETAILS, ctscContactDetails),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT)
        );
    }

    @Test
    public void shouldSuccessfullyApplyJointContentFromCaseDataForGeneratingGeneralOrderDocument() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(GeneralOrderJudgeOrLegalAdvisorType.DEPUTY_DISTRICT_JUDGE);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("contactdivorce@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress("contactdivorce@justice.gov.uk")
            .build();

        assertThat(templateContent).contains(
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(GENERAL_ORDER_DATE, "1 January 2021"),
            entry(GENERAL_ORDER_DETAILS, "some details"),
            entry(GENERAL_ORDER_MADE_BY, "Deputy District Judge some name"),
            entry(SITTING_VENUE, "Petty France, London"),
            entry("sitting", ", sitting"),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(APPLICANT_HEADING, "Applicant 1"),
            entry(RESPONDENT_HEADING, "Applicant 2"),
            entry(GENERAL_ORDER_RECITALS, "test recitals"),
            entry(CTSC_CONTACT_DETAILS, ctscContactDetails),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT)
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
            entry(GENERAL_ORDER_MADE_BY, "an Assistant Justice's Clerk")
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
            entry(GENERAL_ORDER_MADE_BY, "a Proper Officer of the Court")
        );
    }

    @Test
    public void shouldApplyHisHonourJudgeContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(HIS_HONOUR_JUDGE);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "His Honour Judge some name")
        );
    }

    @Test
    public void shouldApplyHerHonourJudgeContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(HER_HONOUR_JUDGE);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "Her Honour Judge some name")
        );
    }
}
