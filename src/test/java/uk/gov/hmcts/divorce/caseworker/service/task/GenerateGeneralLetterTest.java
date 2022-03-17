package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralLetterTemplateContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GenerateGeneralLetterTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private GeneralLetterTemplateContent generalLetterTemplateContent;

    @InjectMocks
    private GenerateGeneralLetter generateLetter;

    @Test
    public void testGenerateLetterToApplicant() {
        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                ENGLISH,
                GENERAL_LETTER_DOCUMENT_NAME
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    public void testGenerateLetterToRespondent() {
        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.RESPONDENT);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                WELSH,
                GENERAL_LETTER_DOCUMENT_NAME
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
