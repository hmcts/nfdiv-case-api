package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.AosResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.AosUndefendedResponseLetterTemplateContent;

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
