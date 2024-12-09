package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.PaperApplicationReceivedTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.notification.PaperApplicationReceivedNotification.LETTER_TYPE_PAPER_APPLICATION_RECEIVED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_PAPER_APPLICATION_RECEIVED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PAPER_APPLICATION_RECEIVED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class PaperApplicationReceivedNotificationTest {
    @Mock
    private PaperApplicationReceivedTemplateContent paperApplicationReceivedTemplateContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @InjectMocks
    private PaperApplicationReceivedNotification notificationHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void testSendToApplicant1Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(paperApplicationReceivedTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
                .thenReturn(templateContent);

        Document document =
                Document.builder()
                        .url("testUrl")
                        .filename("testFileName")
                        .binaryUrl("binaryUrl")
                        .build();

        when(caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                PAPER_APPLICATION_RECEIVED_TEMPLATE_ID,
                ENGLISH,
                NFD_PAPER_APPLICATION_RECEIVED_DOCUMENT_NAME))
                .thenReturn(document);
        notificationHandler.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_PAPER_APPLICATION_RECEIVED);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(document);
        verify(caseDataDocumentService)
                .renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        PAPER_APPLICATION_RECEIVED_TEMPLATE_ID,
                        ENGLISH, NFD_PAPER_APPLICATION_RECEIVED_DOCUMENT_NAME);
    }

    @Test
    void testSendToApplicant2Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(paperApplicationReceivedTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant2()))
            .thenReturn(templateContent);

        Document document =
            Document.builder()
                .url("testUrl")
                .filename("testFileName")
                .binaryUrl("binaryUrl")
                .build();

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            PAPER_APPLICATION_RECEIVED_TEMPLATE_ID,
            WELSH,
            NFD_PAPER_APPLICATION_RECEIVED_DOCUMENT_NAME))
            .thenReturn(document);
        notificationHandler.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_PAPER_APPLICATION_RECEIVED);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(document);
        verify(caseDataDocumentService)
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                PAPER_APPLICATION_RECEIVED_TEMPLATE_ID,
                WELSH, NFD_PAPER_APPLICATION_RECEIVED_DOCUMENT_NAME);
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant("test@test.com", YesOrNo.NO));
        caseData.setApplicant2(createApplicant("testtwo@test.com", YesOrNo.YES));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        return caseData;
    }

    private Applicant createApplicant(String email, YesOrNo languagePreferenceWelsh) {
        return Applicant.builder()
            .email(email)
            .languagePreferenceWelsh(languagePreferenceWelsh)
            .build();
    }
}
