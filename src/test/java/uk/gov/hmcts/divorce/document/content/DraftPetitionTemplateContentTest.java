package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_COSTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_COST_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS_FOR_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class DraftPetitionTemplateContentTest {

    private static final LocalDate LOCAL_DATE = LocalDate.of(2021, 04, 28);

    @InjectMocks
    private DraftPetitionTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForDivorce() {
        CaseData caseData = caseData();

        Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        caseData.setCreatedDate(LocalDate.now(fixedClock));

        Map<String, Object> templateData = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateData).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION_COST, DIVORCE_COSTS),
            entry(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE),
            entry(FINANCIAL_ORDER_CHILD, CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT),
            entry(FINANCIAL_ORDER_OR_DISSOLUTION, CONDITIONAL_ORDER_OF_DIVORCE_FROM),
            entry(HAS_FINANCIAL_ORDERS, false),
            entry(HAS_FINANCIAL_ORDERS_FOR_CHILD, false),
            entry(ISSUE_DATE, "2021-04-28"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(COURT_CASE_DETAILS, null),
            entry(HAS_COST_ORDERS, null),
            entry(MARRIAGE_DATE, null),
            entry(RESPONDENT_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null)
        );
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForDissolution() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

        Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        caseData.setCreatedDate(LocalDate.now(fixedClock));

        Map<String, Object> templateData = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateData).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP),
            entry(DIVORCE_OR_DISSOLUTION_COST, COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP),
            entry(FINANCIAL_ORDER_CHILD, CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT),
            entry(FINANCIAL_ORDER_OR_DISSOLUTION, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH),
            entry(HAS_FINANCIAL_ORDERS, false),
            entry(HAS_FINANCIAL_ORDERS_FOR_CHILD, false),
            entry(ISSUE_DATE, "2021-04-28"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(COURT_CASE_DETAILS, null),
            entry(HAS_COST_ORDERS, null),
            entry(MARRIAGE_DATE, null),
            entry(RESPONDENT_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null)
        );
    }
}
