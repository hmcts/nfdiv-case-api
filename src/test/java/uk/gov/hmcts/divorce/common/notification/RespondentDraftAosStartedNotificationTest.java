package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentDraftAosStartedTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.notification.RespondentDraftAosStartedNotification.RESPONDENT_DRAFTED_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RespondentDraftAosStartedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private RespondentDraftAosStartedTemplateContent respondentDraftAosStartedTemplateContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @InjectMocks
    private RespondentDraftAosStartedNotification notificationHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<Print> printCaptor;


    @Test
    void testSendToApplicant1() {
        CaseData caseData = caseData();
        Long id = 1L;

        var templateVars = getTemplateVars(caseData, caseData.getApplicant1());
        when(commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()
        )).thenReturn(templateVars);

        notificationHandler.sendToApplicant1(caseData, id);

        verify(notificationService).sendEmail(
                eq(caseData.getApplicant1().getEmail()),
                eq(EmailTemplateName.RESPONDENT_DRAFT_AOS_STARTED_APPLICATION),
                anyMap(),
                eq(caseData.getApplicant1().getLanguagePreference()),
                eq(id)
        );

        verify(commonContent).mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendToApplicant1Offline() {

        CaseData caseData = caseData();

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(respondentDraftAosStartedTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
                .thenReturn(templateContent);

        Document respondentDraftAosStarted =
                Document.builder()
                        .url("testUrl")
                        .filename("testFileName")
                        .binaryUrl("binaryUrl")
                        .build();

        when(caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID,
                ENGLISH,
                RESPONDENT_DRAFT_AOS_STARTED_DOCUMENT_NAME))
                .thenReturn(respondentDraftAosStarted);
        notificationHandler.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(RESPONDENT_DRAFTED_AOS);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(respondentDraftAosStarted);
        verify(caseDataDocumentService)
                .renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID,
                        ENGLISH, RESPONDENT_DRAFT_AOS_STARTED_DOCUMENT_NAME);
    }

    private Map<String, String> getTemplateVars(CaseData caseData, Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(NAME, applicant.getFullName());
        templateVars.put(CASE_REFERENCE, TEST_CASE_ID.toString());
        templateVars.put(PARTNER, commonContent.getPartner(caseData, applicant, applicant.getLanguagePreference()));
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }
}
