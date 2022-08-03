package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class GenerateConditionalOrderReminderDocumentTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderReminderContent conditionalOrderReminderContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateConditionalOrderReminderDocument generateConditionalOrderReminderDocument;

    @Test
    void shouldGenerateConditionalOrderReminderDocAndUpdateCaseData() {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderReminderContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateContent);

        final Document document = Document.builder()
            .filename(CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME)
            .build();
        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME, LocalDateTime.now(clock))))
            .thenReturn(document);


        generateConditionalOrderReminderDocument.generateConditionalOrderReminder(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2());

        Assertions.assertThat(caseData.getDocuments().getDocumentsGenerated()).hasSize(1);
    }
}
