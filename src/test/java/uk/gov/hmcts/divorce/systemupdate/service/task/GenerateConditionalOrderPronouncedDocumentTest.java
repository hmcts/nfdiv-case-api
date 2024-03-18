package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderPronouncedTemplateContent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class GenerateConditionalOrderPronouncedDocumentTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @InjectMocks
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Test
    void shouldGenerateConditionalOrderGrantedDocAndUpdateCaseData() {

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        Applicant applicantTest = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .build();
        when(conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicantTest))
            .thenReturn(templateContent);

        generateConditionalOrderPronouncedDocument.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            ENGLISH,
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME);
    }

    @Test
    void shouldGenerateConditionalOrderGrantedDocAndUpdateCaseDataForJudicialSeparation() {

        final Map<String, Object> templateContent = new HashMap<>();
        Applicant applicant1 = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .build();
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID, applicant1))
            .thenReturn(templateContent);

        generateConditionalOrderPronouncedDocument.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID,
            ENGLISH,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME);
    }
}
