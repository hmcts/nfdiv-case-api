package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.DISSOLUTION_OF_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.SECTION;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.SECTION_18A;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.SECTION_18C;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.SPOUSE;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.SPOUSE_OR_CP;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.THE_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.FinalOrderTemplateContent.THE_MARRIAGE_OR_CP;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataForGrantFinalOrder;

@ExtendWith(MockitoExtension.class)
public class FinalOrderTemplateContentTest {
    @Mock
    private Clock clock;

    @InjectMocks
    private FinalOrderTemplateContent finalOrderTemplateContent;

    @BeforeEach
    public void setUp() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
    }

    @Test
    public void shouldMapTemplateContentWhenDivorceCase() {
        var caseData = buildCaseDataForGrantFinalOrder(ApplicationType.SOLE_APPLICATION, DivorceOrDissolution.DIVORCE);

        final Map<String, Object> templateContent = finalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

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

        final Map<String, Object> templateContent = finalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

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
}
