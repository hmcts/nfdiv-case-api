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
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.NocSolsToCitizenNotifications.LETTER_TYPE_INVITE_CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.RESPONDENT_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;

@ExtendWith(MockitoExtension.class)
class NocSolRemovedSelfAsRepresentativeNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig config;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @InjectMocks
    private NocSolRemovedSelfAsRepresentativeNotification notificationHandler;

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
    void testSendToApplicant1OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData, true
        )).thenReturn(getSolTemplateVars(caseData.getApplicant1()));

        notificationHandler.sendToApplicant1OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_SOL_REMOVED_SELF_AS_REPRESENTATIVE),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData, true
        );
    }

    @Test
    void testSendToApplicant2OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData, false
        )).thenReturn(getSolTemplateVars(caseData.getApplicant2()));

        notificationHandler.sendToApplicant2OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_SOL_REMOVED_SELF_AS_REPRESENTATIVE),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData, false
        );
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
            eq(EmailTemplateName.SOL_STOPPED_REP_INVITE_CITIZEN),
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
        caseData.setApplicationType(SOLE_APPLICATION);
        Long id = 1L;

        when(commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getTemplateVars(caseData.getApplicant2()));

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notificationHandler.sendToApplicant2(caseData, id);

        verify(notificationService).sendEmail(
            eq("testtwo@test.com"),
            eq(EmailTemplateName.SOL_STOPPED_REP_INVITE_CITIZEN),
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
        caseData.setApplicationType(SOLE_APPLICATION);

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
            NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID,
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
                NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID,
                ENGLISH, NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME);
    }

    @Test
    void testSendToApplicant2Offline() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = getBasicDocmosisTemplateContent(ENGLISH);

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
            NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID,
            ENGLISH,
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
                NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID,
                ENGLISH, NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME);
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant("test@test.com", "App1 Solicitor Firm", YesOrNo.NO));
        caseData.setApplicant2(createApplicant("testtwo@test.com", "App2 Solicitor Firm", YesOrNo.YES));
        caseData.setApplicationType(JOINT_APPLICATION);
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
