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
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.NocSolsToCitizenNotifications.LETTER_TYPE_INVITE_CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP_INVITE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.RESPONDENT_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;

@ExtendWith(MockitoExtension.class)
class NocSolsToCitizenNotificationsTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @InjectMocks
    private NocSolsToCitizenNotifications notificationHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private EmailTemplatesConfig config;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    private Map<String, String> getTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }

    @Test
    void testSendToApplicant1() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocCitizenTemplateVars(id, caseData.getApplicant1()
        )).thenReturn(getTemplateVars(caseData.getApplicant1()));

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notificationHandler.sendToApplicant1(caseData, id);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateName.NOC_INVITE_CITIZEN),
            argThat(allOf(
                hasEntry(ACCESS_CODE, "12345678"),
                hasEntry(CREATE_ACCOUNT_LINK, SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocCitizenTemplateVars(id, caseData.getApplicant1());
    }

    @Test
    void testSendToApplicant2InSoleApplication() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getTemplateVars(caseData.getApplicant2()));

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notificationHandler.sendToApplicant2(caseData, id);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateName.NOC_INVITE_CITIZEN),
            argThat(allOf(
                hasEntry(ACCESS_CODE, "87654321"),
                hasEntry(CREATE_ACCOUNT_LINK, RESPONDENT_SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocCitizenTemplateVars(id, caseData.getApplicant2());
    }

    @Test
    void testSendToApplicant1Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = getBasicDocmosisTemplateContent(ENGLISH);

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(templateContent);

        Document inviteDocument =
            Document.builder()
                .url("testUrl")
                .filename("testFileName")
                .binaryUrl("binaryUrl")
                .build();

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP_INVITE_TEMPLATE_ID,
            ENGLISH,
            NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME))
            .thenReturn(inviteDocument);
        notificationHandler.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_INVITE_CITIZEN);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(inviteDocument);
        verify(caseDataDocumentService)
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP_INVITE_TEMPLATE_ID,
                ENGLISH, NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME);
    }

    @Test
    void testSendToApplicant2Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = getBasicDocmosisTemplateContent(WELSH);

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant2().getLanguagePreference())).thenReturn(templateContent);

        Document inviteDocument =
            Document.builder()
                .url("testUrl")
                .filename("testFileName")
                .binaryUrl("binaryUrl")
                .build();

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP_INVITE_TEMPLATE_ID,
            WELSH,
            NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME))
            .thenReturn(inviteDocument);
        notificationHandler.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_INVITE_CITIZEN);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(inviteDocument);
        verify(caseDataDocumentService)
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP_INVITE_TEMPLATE_ID,
                WELSH, NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME);
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant(TEST_USER_EMAIL, YesOrNo.NO));
        caseData.setApplicant2(createApplicant(TEST_USER_EMAIL, YesOrNo.YES));
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        CaseInviteApp1 inviteApp1 = CaseInviteApp1.builder()
            .applicant1InviteEmailAddress(caseData.getApplicant1().getEmail())
            .accessCodeApplicant1("12345678")
            .build();
        caseData.setCaseInviteApp1(inviteApp1);

        CaseInvite inviteApp2 = CaseInvite.builder()
            .applicant2InviteEmailAddress(caseData.getApplicant2().getEmail())
            .accessCode("87654321")
            .build();
        caseData.setCaseInvite(inviteApp2);

        return caseData;
    }

    private Applicant createApplicant(String email, YesOrNo languagePreferenceWelsh) {
        return Applicant.builder()
            .email(email)
            .languagePreferenceWelsh(languagePreferenceWelsh)
            .build();
    }
}
