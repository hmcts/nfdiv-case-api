package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.ConditionalOrderAnswersTemplateContent.CO_CONFIRM_INFO_STILL_CORRECT;
import static uk.gov.hmcts.divorce.document.content.templatecontent.ConditionalOrderAnswersTemplateContent.CO_REASON_INFO_NOT_CORRECT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getTemplateFormatDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderAnswersTemplateContentTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderAnswersTemplateContent conditionalOrderAnswersTemplateContent;

    @Test
    void shouldApplyContentFromCaseDataForConditionalOrderAnswers() {

        setMockClock(clock);

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
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry("isSole", true),
            entry("isDivorce", true),
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry("documentDate", getTemplateFormatDate()),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME, APPLICANT_2_FIRST_NAME),
            entry(DocmosisTemplateConstants.APPLICANT_2_LAST_NAME, APPLICANT_2_LAST_NAME),
            entry(APPLICANT_2_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME)
        );
    }

    @Test
    public void shouldSetConfirmInformationCorrectToFalseWithTheReasonWhenSoleApplicationAndInfoIsNotCorrect() {
        setMockClock(clock);

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
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.NO)
                    .reasonInformationNotCorrect("reasons")
                    .build())
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CO_CONFIRM_INFO_STILL_CORRECT, false),
            entry(CO_REASON_INFO_NOT_CORRECT, "reasons")
        );
    }

    @Test
    public void shouldSetConfirmInformationCorrectToTrueWithTheNoReasonWhenSoleApplicationAndInfoIsCorrect() {
        setMockClock(clock);

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
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.YES)
                    .build())
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(entry(CO_CONFIRM_INFO_STILL_CORRECT, true));
        assertThat(result.get(CO_REASON_INFO_NOT_CORRECT)).isNull();
    }

    @Test
    public void shouldSetConfirmInformationCorrectToFalseWithTheReasonWhenJointApplicationAndInfoIsNotCorrect() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.NO)
                    .reasonInformationNotCorrect("reasons 1")
                    .build())
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.NO)
                    .reasonInformationNotCorrect("reasons 2")
                    .build())
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CO_CONFIRM_INFO_STILL_CORRECT, false),
            entry(CO_REASON_INFO_NOT_CORRECT, "reasons 1\nreasons 2")
        );
    }

    @Test
    public void shouldSetConfirmInformationCorrectToFalseWithTheReasonWhenJointApplicationAndOneOfTheApplicantInfoIsNotCorrect() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.YES)
                    .build())
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.NO)
                    .reasonInformationNotCorrect("reasons")
                    .build())
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CO_CONFIRM_INFO_STILL_CORRECT, false),
            entry(CO_REASON_INFO_NOT_CORRECT, "reasons")
        );
    }

    @Test
    public void shouldSetConfirmInformationCorrectToTrueWithTheNoReasonWhenJointApplicationAndInfoIsCorrect() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.YES)
                    .build())
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .confirmInformationStillCorrect(YesOrNo.YES)
                    .build())
                .build())
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = conditionalOrderAnswersTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(entry(CO_CONFIRM_INFO_STILL_CORRECT, true));
        assertThat(result.get(CO_REASON_INFO_NOT_CORRECT)).isNull();
    }
}
