package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getTemplateFormatDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPronouncedTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @Test
    void shouldApplyDivorceContentFromCaseDataForConditionalOrderPronouncedTemplate() {

        LocalDate coPronouncedDate = LocalDate.of(2022, 5, 26);
        LocalDate marriageDate = LocalDate.of(2000, 1, 2);

        setMockClock(clock);

        when(commonContent.getPartner(any(), any())).thenReturn("husband");

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .placeOfMarriage("London")
                    .countryOfMarriage("UK")
                    .date(marriageDate)
                    .build())
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .court(ConditionalOrderCourt.BIRMINGHAM)
                .grantedDate(coPronouncedDate)
                .pronouncementJudge("District Judge")
                .build())
            .build();

        final Map<String, Object> result = conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(IS_SOLE, true),
            entry(IS_DIVORCE, true),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DOCUMENTS_ISSUED_ON, getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(PARTNER, "husband"),
            entry(PLACE_OF_MARRIAGE, "London"),
            entry(COUNTRY_OF_MARRIAGE, "UK"),
            entry(MARRIAGE_DATE, "2 January 2000"),
            entry(JUDGE_NAME, "District Judge"),
            entry(COURT_NAME, "Birmingham Civil and Family Justice Centre"),
            entry(CO_PRONOUNCED_DATE, "26 May 2022"),
            entry(DATE_FO_ELIGIBLE_FROM, "7 July 2022"));
    }
}
