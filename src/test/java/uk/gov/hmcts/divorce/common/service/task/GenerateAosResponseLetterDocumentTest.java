package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.AosResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.AosUndefendedResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GenerateAosResponseLetterDocumentTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private AosResponseLetterTemplateContent aosResponseLetterTemplateContent;

    @Mock
    private AosUndefendedResponseLetterTemplateContent aosUndefendedResponseLetterTemplateContent;

    @Mock
    private GenerateD84Form generateD84Form;

    @Mock
    private GenerateD10Form generateD10Form;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @InjectMocks
    private GenerateAosResponseLetterDocument generateAosResponseLetterDocument;

    @Test
    void shouldGenerateRespondentAnswerDocWhenApplicant1IsOfflineAndIsDisputed() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        doNothing()
            .when(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        final CaseDetails<CaseData, State> result = generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verifyNoMoreInteractions(caseDataDocumentService);
        verifyNoInteractions(aosUndefendedResponseLetterTemplateContent);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateRespondentRespondedDocWhenApplicant1IsOfflineAndUndisputed() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosUndefendedResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verifyNoMoreInteractions(caseDataDocumentService);
        verifyNoInteractions(aosResponseLetterTemplateContent);
    }

    @Test
    void shouldGenerateRespondentRespondedDocWhenApplicant1IsOfflineSolAndDisputedAndJS() {

        final CaseData caseData = caseData();
        caseData.setIsJudicialSeparation(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verify(generateD84Form).generateD84Document(caseData, TEST_CASE_ID);
        verify(coversheetApplicantTemplateContent).apply(caseData, TEST_CASE_ID, caseData.getApplicant1());
        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateRespondentRespondedDocWhenApplicant1IsOfflineAndDisputedAndJS() {

        final CaseData caseData = caseData();
        caseData.setIsJudicialSeparation(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_APP1_JS_SOLE_DISPUTED,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verify(generateD84Form).generateD84Document(caseData, TEST_CASE_ID);
        verify(coversheetApplicantTemplateContent).apply(caseData, TEST_CASE_ID, caseData.getApplicant1());
        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateRespondentRespondedDocsWhenApplicant1SolIsOfflineAndUndisputedAndJS() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);
        caseData.setIsJudicialSeparation(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosUndefendedResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verify(generateD84Form).generateD84Document(caseData, TEST_CASE_ID);
        verify(generateD10Form).apply(caseDetails);

        verifyNoMoreInteractions(caseDataDocumentService);
        verifyNoInteractions(aosResponseLetterTemplateContent);
    }

    @Test
    void shouldNotGenerateRespondentAnswerDocWhenApplicant1IsOfflineAndIsDisputedAndNotJS() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);
        caseData.setIsJudicialSeparation(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(aosResponseLetterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        doNothing()
            .when(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        final CaseDetails<CaseData, State> result = generateAosResponseLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_RESPONSE_LETTER,
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_RESPONSE_LETTER_DOCUMENT_NAME
            );

        verifyNoMoreInteractions(caseDataDocumentService);
        verifyNoInteractions(aosUndefendedResponseLetterTemplateContent);
        verifyNoMoreInteractions(generateD84Form);
        verifyNoMoreInteractions(coversheetApplicantTemplateContent);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldNotGenerateAnyDocWhenApplicant1IsNotOffline() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        generateAosResponseLetterDocument.apply(caseDetails);

        verifyNoInteractions(caseDataDocumentService, aosResponseLetterTemplateContent, aosUndefendedResponseLetterTemplateContent);
    }
}
