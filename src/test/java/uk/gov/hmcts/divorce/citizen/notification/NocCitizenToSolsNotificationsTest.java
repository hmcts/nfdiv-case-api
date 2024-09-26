package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.LitigantGrantOfRepresentationConfirmationTemplateContent;
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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.LETTER_TYPE_GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;

@ExtendWith(MockitoExtension.class)
class NocCitizenToSolsNotificationsTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private LitigantGrantOfRepresentationConfirmationTemplateContent litigantConfirmationTemplateContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @InjectMocks
    private NocCitizenToSolsNotifications notificationHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    private Map<String, String> getSolTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = getTemplateVars(applicant);
        templateVars.put(SOLICITOR_REFERENCE, applicant.getSolicitor().getReference());
        return templateVars;
    }

    private Map<String, String> getTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }

    @Test
    void testSendToApplicant1() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocCitizenTemplateVars(id, caseData.getApplicant1()
        )).thenReturn(getTemplateVars(caseData.getApplicant1()));

        notificationHandler.sendToApplicant1(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getEmail()),
            eq(EmailTemplateName.NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocCitizenTemplateVars(id, caseData.getApplicant1());
    }

    @Test
    void testSendToApplicant1Solicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocSolsTemplateVars(id, caseData.getApplicant1()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant1()));

        notificationHandler.sendToApplicant1Solicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_NEW_SOL),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocSolsTemplateVars(id, caseData.getApplicant1()
        );
    }


    @Test
    void testSendToApplicant2() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getTemplateVars(caseData.getApplicant2()));

        notificationHandler.sendToApplicant2(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getEmail()),
            eq(EmailTemplateName.NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocCitizenTemplateVars(id, caseData.getApplicant2());
    }

    @Test
    void testSendToApplicant1Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(litigantConfirmationTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
                .thenReturn(templateContent);

        Document nocConfirmationDocument =
                Document.builder()
                        .url("testUrl")
                        .filename("testFileName")
                        .binaryUrl("binaryUrl")
                        .build();

        when(caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                ENGLISH,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME))
                .thenReturn(nocConfirmationDocument);
        notificationHandler.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GRANT_OF_REPRESENTATION);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(nocConfirmationDocument);
        verify(caseDataDocumentService)
                .renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                        ENGLISH, NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME);
    }

    @Test
    void testSendToApplicant2Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = new HashMap<>();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(litigantConfirmationTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant2()))
                .thenReturn(templateContent);

        Document nocConfirmationDocument =
                Document.builder()
                        .url("testUrl")
                        .filename("testFileName")
                        .binaryUrl("binaryUrl")
                        .build();

        when(caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                WELSH,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME))
                .thenReturn(nocConfirmationDocument);

        notificationHandler.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GRANT_OF_REPRESENTATION);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(nocConfirmationDocument);
        verify(caseDataDocumentService)
                .renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                        WELSH, NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME);
    }


    @Test
    void testSendToApplicant2Solicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocSolsTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant2()));

        notificationHandler.sendToApplicant2Solicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_NEW_SOL),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocSolsTemplateVars(id, caseData.getApplicant2()
        );
    }

    @Test
    void testSendToApplicant1OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData.getApplicant1()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant1()));

        notificationHandler.sendToApplicant1OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData.getApplicant1()
        );
    }

    @Test
    void testSendToApplicant2OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant2()));

        notificationHandler.sendToApplicant2OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData.getApplicant2()
        );
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant("test@test.com", "App1 Solicitor Firm", YesOrNo.NO));
        caseData.setApplicant2(createApplicant("testtwo@test.com", "App2 Solicitor Firm", YesOrNo.YES));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        return caseData;
    }

    private Applicant createApplicant(String email, String solicitorFirmName, YesOrNo languagePreferenceWelsh) {
        Solicitor solicitor = createSolicitor(solicitorFirmName);
        return Applicant.builder()
            .email(email)
            .languagePreferenceWelsh(languagePreferenceWelsh)
            .solicitor(solicitor)
            .build();
    }

    private Solicitor createSolicitor(String firmName) {
        return Solicitor.builder()
            .email("test@example.com")
            .firmName(firmName)
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().organisationId(TEST_ORG_ID).build())
                .build())
            .build();
    }

}
