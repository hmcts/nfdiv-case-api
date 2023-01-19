package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class JudicialSeparationSwitchToSoleSolicitorContentTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;


    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private JudicialSeparationSwitchToSoleSolicitorContent generateJudicialSeparationSwitchToSoleSolicitorLetter;

    @Test
    void shouldGenerateSwitchToSoleLetterToSolicitorForJudicialSeparation() {
        CaseData caseData = caseData();

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();

        final Applicant respondent = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();
        caseData.setApplicant2(respondent);

        setMockClock(clock);

        final Map<String, Object> expectedTemplateContent = getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference())).thenReturn(expectedTemplateContent);


        expectedTemplateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS);
        expectedTemplateContent.put(APPLICANT_1_FULL_NAME, "applicant1 Full Name");
        expectedTemplateContent.put(APPLICANT_2_FULL_NAME, "respondent Full Name");

        expectedTemplateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, "Marriage");

        expectedTemplateContent.put(SOLICITOR_REFERENCE, TEST_REFERENCE);

        expectedTemplateContent.put(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        generateJudicialSeparationSwitchToSoleSolicitorLetter.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), respondent);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            expectedTemplateContent,
            TEST_CASE_ID,
            JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }
}

