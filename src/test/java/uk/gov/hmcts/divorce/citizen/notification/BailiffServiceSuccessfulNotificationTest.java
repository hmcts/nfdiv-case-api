package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffServiceSuccessfulTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.BailiffServiceSuccessfulNotification.BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.citizen.notification.BailiffServiceSuccessfulNotification.BAILIFF_SERVICE_SUCCESSFUL_LETTER_ID;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived.BAILIFF_SERVICE_SUCCESSFUL_CONFIDENTIAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_SUCCESSFUL_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_SUCCESSFUL;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class BailiffServiceSuccessfulNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private BailiffServiceSuccessfulTemplateContent bailiffServiceSuccessfulTemplateContent;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @InjectMocks
    private BailiffServiceSuccessfulNotification notification;

    @Test
    void shouldSendNotificationToApplicantWithDivorceContent() {
        CaseData caseData = validApplicant1CaseData();
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_SUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicantWithDivorceContentWhenLangPrefIsWelsh() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_SUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicantWithDissolutionContent() {
        CaseData caseData = validApplicant1CaseData();
        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(IS_DISSOLUTION, CommonContent.YES);
        templateVars.put(IS_DIVORCE, NO);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_SUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicantWithDissolutionContentWhenLangPrefIsWelsh() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(IS_DISSOLUTION, CommonContent.YES);
        templateVars.put(IS_DIVORCE, NO);
        templateVars.put(PARTNER, "partner sifil");
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_SUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, CommonContent.YES),
                hasEntry(PARTNER, "partner sifil")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldNotSendOfflineLetterToApplicant1WhenJudicialSeparationCase() {
        CaseData caseData = validApplicant1CaseData();
        caseData.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldSendOfflineLetterToApplicant1WhenNotJudicialSeparationCase() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getAlternativeService().getBailiff().setCertificateOfServiceDocument(DivorceDocument.builder().build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(bailiffServiceSuccessfulTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
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
            BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
            ENGLISH,
            BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME))
            .thenReturn(document);
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(BAILIFF_SERVICE_SUCCESSFUL_LETTER_ID);
        assertThat(print.getLetters()).hasSize(2);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(document);
        verify(caseDataDocumentService)
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
                ENGLISH, BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME);
    }

    @Test
    void shouldAddDocumentToGeneratedDocumentsWhenApplicant1IsNotConfidential() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getAlternativeService().getBailiff().setCertificateOfServiceDocument(DivorceDocument.builder().build());
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PUBLIC);

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(bailiffServiceSuccessfulTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
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
            BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
            ENGLISH,
            BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME))
            .thenReturn(document);
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        assertThat(caseData.getDocuments().getDocumentsGenerated()).hasSize(1);
        assertThat(caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentLink()).isEqualTo(document);
        assertThat(caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType())
            .isEqualTo(BAILIFF_SERVICE_SUCCESSFUL_LETTER);
    }

    @Test
    void shouldAddDocumentToConfidentialGeneratedDocumentsWhenApplicant1IsConfidential() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getAlternativeService().getBailiff().setCertificateOfServiceDocument(DivorceDocument.builder().build());
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(bailiffServiceSuccessfulTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
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
            BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
            ENGLISH,
            BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME))
            .thenReturn(document);
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        assertThat(caseData.getDocuments().getDocumentsGenerated()).isNull();
        assertThat(caseData.getDocuments().getConfidentialDocumentsGenerated()).hasSize(1);
        assertThat(caseData.getDocuments().getConfidentialDocumentsGenerated().get(0).getValue().getDocumentLink()).isEqualTo(document);
        assertThat(caseData.getDocuments().getConfidentialDocumentsGenerated().get(0).getValue().getConfidentialDocumentsReceived())
            .isEqualTo(BAILIFF_SERVICE_SUCCESSFUL_CONFIDENTIAL_LETTER);
    }
}
