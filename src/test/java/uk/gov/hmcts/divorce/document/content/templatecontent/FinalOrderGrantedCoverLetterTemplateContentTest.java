package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class FinalOrderGrantedCoverLetterTemplateContentTest {

    private static final String NAME = "name";
    public static final String CASE_REFERENCE = "caseReference";


    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private FinalOrderGrantedCoverLetterTemplateContent finalOrderGrantedCoverLetterTemplateContent;

    @Test
    public void shouldBeAbleToHandleFinalOrderCoverLetterTemplate() {
        assertThat(finalOrderGrantedCoverLetterTemplateContent.getSupportedTemplates()).containsOnly(FINAL_ORDER_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    public void shouldProvideCorrectTemplateContentForFoGrantedCoverLetter() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        setMockClock(clock);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));

        final var templateContent = finalOrderGrantedCoverLetterTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsAllEntriesOf(
            Map.of(
                NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME,
                DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT,
                CASE_REFERENCE, formatId(TEST_CASE_ID)
            )
        );
    }
}
