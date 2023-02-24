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
import uk.gov.hmcts.divorce.document.content.AosOverdueLetterTemplateContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GenerateAosOverdueLetterDocumentTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private AosOverdueLetterTemplateContent letterTemplateContent;

    @InjectMocks
    private GenerateAosOverdueLetterDocument generateAosOverdueLetterDocument;

    @Test
    void shouldGenerateAosOverdueDoc() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(letterTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        doNothing()
            .when(caseDataDocumentService).renderDocumentAndUpdateCaseData(
                caseData,
                AOS_OVERDUE_LETTER,
                templateContent,
                TEST_CASE_ID,
                AOS_OVERDUE_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_OVERDUE_LETTER_DOCUMENT_NAME
            );

        final CaseDetails<CaseData, State> result = generateAosOverdueLetterDocument.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                AOS_OVERDUE_LETTER,
                templateContent,
                TEST_CASE_ID,
                AOS_OVERDUE_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                AOS_OVERDUE_LETTER_DOCUMENT_NAME
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
