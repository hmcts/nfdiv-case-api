package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentAnswersTemplateContent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GenerateRespondentAnswersDocTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private RespondentAnswersTemplateContent templateContent;

    @InjectMocks
    private GenerateRespondentAnswersDoc generateRespondentAnswersDoc;

    @Test
    void shouldGenerateRespondentAnswerDocWhenTaskIsExecuted() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE.atStartOfDay());

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE))
            .thenReturn(templateContentSupplier);

        doNothing()
            .when(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                RESPONDENT_ANSWERS,
                templateContentSupplier,
                TEST_CASE_ID,
                RESPONDENT_ANSWERS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                RESPONDENT_ANSWERS_DOCUMENT_NAME
            );

        final CaseDetails<CaseData, State> result = generateRespondentAnswersDoc.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                RESPONDENT_ANSWERS,
                templateContentSupplier,
                TEST_CASE_ID,
                RESPONDENT_ANSWERS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                RESPONDENT_ANSWERS_DOCUMENT_NAME
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
