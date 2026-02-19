package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getTemplateFormatDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class RespondentFinalOrderAnswersTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private RespondentFinalOrderAnswersTemplateContent respondentFinalOrderAnswersTemplateContent;

    @Test
    void shouldApplyContentFromCaseDataForFinalOrderAnswersWhenRespondentNotRepresented() {

        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .solicitorRepresented(YesOrNo.NO)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .finalOrder(FinalOrder.builder().applicant2FinalOrderExplanation("Test").build())
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        final Map<String, Object> result = respondentFinalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry("isDivorce", true),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry("documentDate", getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry("reason", "Test")
        );
    }

    @Test
    void shouldApplyContentFromCaseDataForFinalOrderAnswersWhenRespondentRepresented() {

        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .solicitorRepresented(YesOrNo.YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .finalOrder(FinalOrder.builder().applicant2SolFinalOrderWhyNeedToApply("Solicitor Explanation").build())
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        final Map<String, Object> result = respondentFinalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry("isDivorce", true),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry("documentDate", getTemplateFormatDate()),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry("reason", "Solicitor Explanation")
        );
    }
}
