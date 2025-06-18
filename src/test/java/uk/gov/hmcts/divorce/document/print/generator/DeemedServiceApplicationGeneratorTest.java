package uk.gov.hmcts.divorce.document.print.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DeemedServiceApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_SERVICE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;

@ExtendWith(MockitoExtension.class)
class DeemedServiceApplicationGeneratorTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DeemedServiceApplicationTemplateContent templateContent;

    @InjectMocks
    private DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;

    @Test
    void shouldRenderDocumentWithDeemedTemplateVariables() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build()).build();

        Map<String, Object> templateVariables = Collections.emptyMap();

        when(templateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
            .thenReturn(templateVariables);

        Document generatedDocument = Document.builder().filename(TEST_REFERENCE).build();
        when(caseDataDocumentService.renderDocument(
            templateVariables,
            TEST_CASE_ID,
            DEEMED_SERVICE_APPLICATION_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            DEEMED_SERVICE_APPLICATION_DOCUMENT_NAME
        )).thenReturn(generatedDocument);

        DivorceDocument result = deemedServiceApplicationGenerator.generateDocument(
            caseId, caseData.getApplicant1(), caseData
        );

        verify(caseDataDocumentService).renderDocument(
            templateVariables,
            TEST_CASE_ID,
            DEEMED_SERVICE_APPLICATION_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            DEEMED_SERVICE_APPLICATION_DOCUMENT_NAME
        );
        assertThat(result.getDocumentLink()).isEqualTo(generatedDocument);
        assertThat(result.getDocumentType()).isEqualTo(DEEMED_SERVICE);
        assertThat(result.getDocumentFileName()).isEqualTo(TEST_REFERENCE);
    }
}
