package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralLetterTemplateContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GenerateGeneralLetterTest {

    private static final LocalDate DATE = LocalDate.of(2022, 3, 16);
    private static final String FILE_NAME = "GeneralLetter-2022-03-16:00:00";

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private GeneralLetterTemplateContent generalLetterTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateGeneralLetter generateLetter;

    @BeforeEach
    public void setUp() {
        setMockClock(clock, DATE);
    }

    @Test
    public void shouldGenerateGeneralLetterAndUpdateCaseDataWithGeneralLettersWithAttachments() {

        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                DocumentType.GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                ENGLISH,
                FILE_NAME
        );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    public void shouldGenerateGeneralLetterAndUpdateCaseDataWithGeneralLettersWithoutAttachments() {

        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        caseData.getGeneralLetter().setGeneralLetterAttachments(null);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                DocumentType.GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                ENGLISH,
                FILE_NAME
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

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2()
                .getLanguagePreference())).thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                DocumentType.GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                WELSH,
                FILE_NAME
        );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    public void testGenerateLetterToConfidentialApplicant() {
        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalLetterTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(templateContent);

        final var result = generateLetter.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                DocumentType.GENERAL_LETTER,
                templateContent,
                TEST_CASE_ID,
                GENERAL_LETTER_TEMPLATE_ID,
                ENGLISH,
                FILE_NAME
        );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}