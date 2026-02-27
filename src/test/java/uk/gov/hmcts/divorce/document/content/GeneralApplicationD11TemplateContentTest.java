package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationHearingNotRequired;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_APPLICATION_D11_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationD11TemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private GeneralApplicationD11TemplateContent templateContent;

    private CaseData caseData;
    private Map<String,Object> expectedEntries;

    @Test
    void shouldReturnExpectedTemplateContentForHappyPathApplicant1Application() {
        setupDummyDataAndMocks();

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getGeneralApplication()
        );

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnExpectedTemplateContentForDocsAndEvidenceNotProvided() {
        setupDummyDataAndMocks();

        final InterimApplicationOptions interimApplicationOptions = caseData.getApplicant1().getInterimApplicationOptions();
        final GeneralApplicationD11JourneyOptions generalApplicationAnswers = interimApplicationOptions
            .getGeneralApplicationD11JourneyOptions();
        interimApplicationOptions.setInterimAppsCannotUploadDocs(YesOrNo.YES);
        interimApplicationOptions.setInterimAppsCanUploadEvidence(YesOrNo.NO);
        generalApplicationAnswers.setCannotUploadAgreedEvidence(YesOrNo.YES);

        expectedEntries.put("hasProvidedStatement", "No");
        expectedEntries.put("hasProvidedEvidence", "No");
        expectedEntries.put("supportingEvidenceUploaded", "No");
        expectedEntries.put("hasUploadedPartnerAgreesDocs", "No");

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getGeneralApplication()
        );

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldGetSupportedTemplates() {
        assertThat(templateContent.getSupportedTemplates()).containsOnly(GENERAL_APPLICATION_D11_TEMPLATE_ID);
    }

    private void buildD11GeneralApplication(CaseData caseData) {
        caseData.getApplicant1().getInterimApplicationOptions().setGeneralApplicationD11JourneyOptions(
            GeneralApplicationD11JourneyOptions.builder()
                .hearingNotRequired(GeneralApplicationHearingNotRequired.YES_PARTNER_AGREES_WITH_NO_HEARING)
                .cannotUploadAgreedEvidence(null)
                .partnerDetailsCorrect(YesOrNo.YES)
                .type(GeneralApplicationType.OTHER)
                .typeOtherDetails("Other details")
                .reason("I need to apply")
                .statementOfEvidence("statement")
                .build());

        caseData.getApplicant1().getInterimApplicationOptions().setInterimAppsCanUploadEvidence(YesOrNo.YES);
    }

    private Map<String,Object> buildExpectedTemplateContent(CaseData caseData) {
        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", caseData.getApplicant2().getFullName());
        expectedEntries.put("applicationDate", "1 August 2025");
        expectedEntries.put("applicationType", "Something else");
        expectedEntries.put("applicationTypeOtherDetails", "Other details");
        expectedEntries.put("applicationReason", "I need to apply");
        expectedEntries.put("statementOfEvidence", "statement");
        expectedEntries.put("caseApplicantLabel", "Applicant");
        expectedEntries.put("caseRespondentOrApplicant2Label", "Respondent");
        expectedEntries.put("generalApplicantLabel", "Applicant");
        expectedEntries.put("hasProvidedEvidence", "Yes");
        expectedEntries.put("hasProvidedStatement", "Yes");
        expectedEntries.put("isOtherApplicationType", "Yes");
        expectedEntries.put("evidencePartnerAgreesRequired", "Yes");
        expectedEntries.put("generalApplicantFullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("supportingEvidenceUploaded", "Yes");
        expectedEntries.put("partnerDetailsCorrect", "Yes");
        expectedEntries.put("hearingNotRequiredDetails", "Yes, because my partner agrees to this being dealt with without a hearing");
        expectedEntries.put("hasUploadedPartnerAgreesDocs", "Yes");
        expectedEntries.put("divorceAndDissolutionHeader","Divorce and Dissolution");
        expectedEntries.put("phoneAndOpeningTimes","0300 303 0642 (Monday to Friday, 10am to 6pm)");
        expectedEntries.put("courtsAndTribunalsServiceHeader","HM Courts & Tribunals Service");
        expectedEntries.put("contactEmail","contactdivorce@justice.gov.uk");

        return expectedEntries;
    }

    private void setupDummyDataAndMocks() {
        caseData = caseData();
        caseData.getGeneralApplication().setGeneralApplicationReceivedDate(LocalDateTime.of(2025, Month.AUGUST, 1, 0, 0));
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        buildD11GeneralApplication(caseData);
        expectedEntries = buildExpectedTemplateContent(caseData);

        final Map<String, Object> basicDocmosisTemplateContent = getBasicDocmosisTemplateContent(ENGLISH);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(basicDocmosisTemplateContent);
        when(docmosisCommonContent.getGeneralApplicationTypeLabel(
            GeneralApplicationType.OTHER, true
        )).thenReturn("Something else");
    }
}
