package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.DISSOLUTION_OF_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.DISSOLUTION_OF_A_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.DIVORCE_CY;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.FORMER_CIVIL_PARTNER_CY;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SECTION;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SECTION_18A;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SECTION_18C;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SPOUSE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SPOUSE_CY;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.SPOUSE_OR_CP;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.THE_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.FinalOrderGrantedTemplateContent.THE_MARRIAGE_OR_CP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataForGrantFinalOrder;

@ExtendWith(MockitoExtension.class)
public class FinalOrderGrantedTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private FinalOrderGrantedTemplateContent finalOrderGrantedTemplateContent;

    @Test
    public void shouldMapTemplateContentWhenDivorceCase() {
        var caseData = buildCaseDataForGrantFinalOrder(ApplicationType.SOLE_APPLICATION, DivorceOrDissolution.DIVORCE);
        caseData.getFinalOrder().setGrantedDate(LocalDateTime.of(2022, 3, 16, 0, 0));

        final Map<String, Object> templateContent = finalOrderGrantedTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            null);

        assertThat(templateContent).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, "16 March 2022"),
            entry(IS_SOLE, true),
            entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(CO_PRONOUNCED_DATE, "10 March 2022"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_DATE, "10 June 1990"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "United Kingdom"),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE),
            entry(THE_MARRIAGE_OR_CP, THE_MARRIAGE),
            entry(SECTION, SECTION_18A),
            entry(SPOUSE_OR_CP, SPOUSE)
        );
    }

    @Test
    public void shouldMapTemplateContentWhenDissolutionCase() {
        var caseData = buildCaseDataForGrantFinalOrder(ApplicationType.JOINT_APPLICATION, DivorceOrDissolution.DISSOLUTION);
        caseData.getFinalOrder().setGrantedDate(LocalDateTime.of(2022, 3, 16, 0, 0));

        final Map<String, Object> templateContent = finalOrderGrantedTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            null);

        assertThat(templateContent).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, "16 March 2022"),
            entry(IS_SOLE, false),
            entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(CO_PRONOUNCED_DATE, "10 March 2022"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_DATE, "10 June 1990"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "United Kingdom"),
            entry(DIVORCE_OR_DISSOLUTION, DISSOLUTION_OF_A_CIVIL_PARTNERSHIP),
            entry(THE_MARRIAGE_OR_CP, A_CIVIL_PARTNERSHIP),
            entry(SECTION, SECTION_18C),
            entry(SPOUSE_OR_CP, CIVIL_PARTNER)
        );
    }

    @Test
    public void shouldMapTemplateContentInWelshWhenDivorceCase() {
        var caseData = buildCaseDataForGrantFinalOrder(ApplicationType.SOLE_APPLICATION, DivorceOrDissolution.DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getFinalOrder().setGrantedDate(LocalDateTime.of(2022, 3, 16, 0, 0));

        final Map<String, Object> templateContent = finalOrderGrantedTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            null);

        assertThat(templateContent).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, "16 March 2022"),
            entry(IS_SOLE, true),
            entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(CO_PRONOUNCED_DATE, "10 March 2022"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY),
            entry(MARRIAGE_DATE, "10 June 1990"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "United Kingdom"),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE_CY),
            entry(THE_MARRIAGE_OR_CP, MARRIAGE_CY),
            entry(SECTION, SECTION_18A),
            entry(SPOUSE_OR_CP, SPOUSE_CY)
        );
    }

    @Test
    public void shouldMapTemplateContentInWelshWhenDissolutionCase() {
        var caseData = buildCaseDataForGrantFinalOrder(ApplicationType.JOINT_APPLICATION, DivorceOrDissolution.DISSOLUTION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getFinalOrder().setGrantedDate(LocalDateTime.of(2022, 3, 16, 0, 0));

        final Map<String, Object> templateContent = finalOrderGrantedTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            null);

        assertThat(templateContent).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, "16 March 2022"),
            entry(IS_SOLE, false),
            entry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name"),
            entry(CO_PRONOUNCED_DATE, "10 March 2022"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY),
            entry(MARRIAGE_DATE, "10 June 1990"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "United Kingdom"),
            entry(DIVORCE_OR_DISSOLUTION, DISSOLUTION_OF_A_CIVIL_PARTNERSHIP_CY),
            entry(THE_MARRIAGE_OR_CP, CIVIL_PARTNERSHIP_CY),
            entry(SECTION, SECTION_18C),
            entry(SPOUSE_OR_CP, FORMER_CIVIL_PARTNER_CY)
        );
    }

    @Test
    public void shouldGetSupportedTemplates() {
        assertThat(finalOrderGrantedTemplateContent.getSupportedTemplates()).containsOnly(FINAL_ORDER_TEMPLATE_ID);
    }
}
