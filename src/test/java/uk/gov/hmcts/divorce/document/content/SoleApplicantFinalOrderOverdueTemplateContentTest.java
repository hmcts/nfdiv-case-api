package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SOLE_APPLICANT_FINAL_ORDER_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class SoleApplicantFinalOrderOverdueTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private SoleApplicantFinalOrderOverdueTemplateContent soleApplicantFinalOrderOverdueTemplateContent;

    @Test
    void shouldBeAbleToHandleLitigantGrantOfRepresentationConfirmationTemplate() {
        assertThat(soleApplicantFinalOrderOverdueTemplateContent.getSupportedTemplates())
                .containsAll(List.of(SOLE_APPLICANT_FINAL_ORDER_OVERDUE_TEMPLATE_ID));
    }

    @Test
    void shouldMapTemplateContentWhenRecipientIsApplicant1() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
        CaseData caseData = TestDataHelper.caseData();
        caseData.setApplicant1(TestDataHelper.getApplicantWithAddress(FEMALE));
        caseData.getConditionalOrder().setGrantedDate(LocalDate.of(2021, 3, 16));

        LocalDate finalOrderOverdueDate = LocalDate.of(2022, 3, 16);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = soleApplicantFinalOrderOverdueTemplateContent
                .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).containsAnyOf(
                entry(FIRST_NAME, TEST_FIRST_NAME),
                entry(LAST_NAME, TEST_LAST_NAME),
                entry(ADDRESS, AddressUtil.getPostalAddress(caseData.getApplicant1().getAddress())),
                entry(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER)),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(IS_DIVORCE,  caseData.isDivorce()),
                entry(FINAL_ORDER_OVERDUE_DATE, finalOrderOverdueDate)
        );
    }
}
