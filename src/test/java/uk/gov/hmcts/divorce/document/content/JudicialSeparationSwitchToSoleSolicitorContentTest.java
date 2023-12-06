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
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
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
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private JudicialSeparationSwitchToSoleSolicitorContent generateJudicialSeparationSwitchToSoleSolicitorLetter;

    @Test
    void shouldGenerateSwitchToSoleLetterWhenApp2isRepresentedAndNoSolicitorReference() {

        CaseData caseData = caseData();

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName("test first name")
            .lastName("test last name")
            .solicitorRepresented(NO)
            .build();

        caseData.setApplicant1(applicant);

        final Applicant respondent = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName("test first name")
            .lastName("test last name")
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .build()
            )
            .build();
        caseData.setApplicant2(respondent);

        setMockClock(clock);



        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(applicant.getLanguagePreference()));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), ENGLISH)).thenReturn("husband");

        final Map<String, Object> expectedTemplateContent = getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        expectedTemplateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS);
        expectedTemplateContent.put(APPLICANT_1_FULL_NAME, "test first name test last name");
        expectedTemplateContent.put(RESPONDENT_FULL_NAME, "test first name test last name");

        expectedTemplateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage");
        expectedTemplateContent.put(RELATION, "husband");

        expectedTemplateContent.put(SOLICITOR_REFERENCE, NOT_PROVIDED);

        expectedTemplateContent.put(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED);

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



    @Test
    void shouldGenerateSwitchToSoleLetterWhenBothApplicantsAreRepresented() {

        CaseData caseData = caseData();

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName("test first name")
            .lastName("test last name")
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();

        caseData.setApplicant1(applicant);

        final Applicant respondent = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName("test first name")
            .lastName("test last name")
            .solicitorRepresented(YES)
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

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(applicant.getLanguagePreference()));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), ENGLISH)).thenReturn("husband");

        final Map<String, Object> expectedTemplateContent = getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        expectedTemplateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedTemplateContent.put(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS);
        expectedTemplateContent.put(APPLICANT_1_FULL_NAME, "test first name test last name");
        expectedTemplateContent.put(RESPONDENT_FULL_NAME, "test first name test last name");
        expectedTemplateContent.put(RELATION, "husband");
        expectedTemplateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage");
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

