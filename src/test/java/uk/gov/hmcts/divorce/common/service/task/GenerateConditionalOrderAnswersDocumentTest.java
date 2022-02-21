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
import uk.gov.hmcts.divorce.document.content.ConditionalOrderAnswersTemplateContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GenerateConditionalOrderAnswersDocumentTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderAnswersTemplateContent conditionalOrderAnswersTemplateContent;

    @InjectMocks
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Test
    void shouldGenerateConditionalOrderAnswersDocument() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(conditionalOrderAnswersTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        final CaseDetails<CaseData, State> result = generateConditionalOrderAnswersDocument.apply(caseDetails);

        assertThat(result.getData()).isEqualTo(caseData);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                CONDITIONAL_ORDER_ANSWERS,
                templateContent,
                TEST_CASE_ID,
                CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME
            );
    }
}