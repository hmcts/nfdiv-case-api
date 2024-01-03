package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.templatecontent.RespondentAnswersTemplateContent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.A_DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.A_DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_DIVORCE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_A_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DISPUTING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_DIVORCE_CY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class RespondentAnswersTemplateContentTest {

    @InjectMocks
    private RespondentAnswersTemplateContent respondentAnswersTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForRespondentAnswers() {

        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setFirstName("app2fname");
        caseData.getApplicant2().setLastName("app2lname");

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "28 April 2021"),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(APPLICANT_2_FULL_NAME, "app2fname app2lname"),
            entry("respJurisdictionAgree", YES.getValue()),
            entry("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "jurisdiction reason"),
            entry("inWhichCountryIsYourLifeMainlyBased", "Country"),
            entry("respLegalProceedingsExist", YES.getValue()),
            entry("respLegalProceedingsDescription", "some description"),
            entry("respSolicitorRepresented", YES.getValue())
        );
    }

    @Test
    void shouldGenerateDisputingDivorceContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(THE_APPLICATION, A_DIVORCE_APPLICATION),
            entry(IS_DISPUTING, DISPUTING_DIVORCE)
        );
    }

    @Test
    void shouldGenerateDisputingCivilPartnershipContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);
        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(THE_APPLICATION, END_A_CIVIL_PARTNERSHIP),
            entry(IS_DISPUTING, DISPUTING_CIVIL_PARTNERSHIP)
        );
    }

    @Test
    void shouldGenerateNotDisputingDivorceContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(THE_APPLICATION, A_DIVORCE_APPLICATION),
            entry(IS_DISPUTING, WITHOUT_DISPUTING_DIVORCE)
        );
    }

    @Test
    void shouldGenerateNotDisputingCivilPartnershipContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(THE_APPLICATION, END_A_CIVIL_PARTNERSHIP),
            entry(IS_DISPUTING, WITHOUT_DISPUTING_CIVIL_PARTNERSHIP)
        );
    }

    @Test
    void shouldGenerateDisputingWelshDivorceContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY),
            entry(THE_APPLICATION, A_DIVORCE_APPLICATION_CY),
            entry(IS_DISPUTING, DISPUTING_DIVORCE_CY)
        );
    }

    @Test
    void shouldGenerateDisputingWelshCivilPartnershipContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);
        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY),
            entry(THE_APPLICATION, END_A_CIVIL_PARTNERSHIP_CY),
            entry(IS_DISPUTING, DISPUTING_CIVIL_PARTNERSHIP_CY)
        );
    }

    @Test
    void shouldGenerateNotDisputingWelshDivorceContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY),
            entry(THE_APPLICATION, A_DIVORCE_APPLICATION_CY),
            entry(IS_DISPUTING, WITHOUT_DISPUTING_DIVORCE_CY)
        );
    }

    @Test
    void shouldGenerateNotDisputingWelshCivilPartnershipContent() {
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LOCAL_DATE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        acknowledgementOfService.setJurisdictionAgree(YES);
        acknowledgementOfService.setReasonCourtsOfEnglandAndWalesHaveNoJurisdiction("jurisdiction reason");
        acknowledgementOfService.setInWhichCountryIsYourLifeMainlyBased("Country");
        acknowledgementOfService.setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1Lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        final Map<String, Object> templateContent = respondentAnswersTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY),
            entry(THE_APPLICATION, END_A_CIVIL_PARTNERSHIP_CY),
            entry(IS_DISPUTING, WITHOUT_DISPUTING_CIVIL_PARTNERSHIP_CY)
        );
    }
}
