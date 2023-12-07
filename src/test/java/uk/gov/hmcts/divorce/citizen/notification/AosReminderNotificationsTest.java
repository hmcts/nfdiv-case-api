package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosOverdueDocumentPack;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class AosReminderNotificationsTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private AosOverdueDocumentPack aosOverdueDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private AosReminderNotifications aosReminderNotifications;

    @Test
    void shouldSendReminderEmailToSoleRespondentWithDivorceContentIfNotSolicitorService() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplication().setServiceMethod(COURT_SERVICE);
        data.getApplicant2().setEmail(null);
        data.setCaseInvite(new CaseInvite("applicant2@test.com", ACCESS_CODE, null));

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_REMINDER, YES),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendReminderEmailToSoleRespondentWithDissolutionContentIfNotSolicitorService() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplication().setServiceMethod(COURT_SERVICE);
        data.getApplicant2().setEmail(null);
        data.setCaseInvite(new CaseInvite("applicant2@test.com", ACCESS_CODE, null));

        Map<String, String> dissolutionTemplateVars = new HashMap<>();
        dissolutionTemplateVars.putAll(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_REMINDER, YES),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendReminderEmailToSoleRespondentIfSolicitorService() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        data.getApplicant2().setEmail(null);
        data.setCaseInvite(new CaseInvite(null, ACCESS_CODE, null));

        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService, commonContent);
    }

    @Test
    void shouldNotSendReminderEmailToSoleRespondentIfNoApplicant2Email() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant2().setEmail(null);
        data.setCaseInvite(new CaseInvite(null, ACCESS_CODE, null));

        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendReminderEmailToSoleRespondentIfNoAccessCode() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant2().setEmail(null);
        data.setCaseInvite(new CaseInvite(null, null, null));

        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendPartnerNotRespondedToSoleApplicantEmail() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        aosReminderNotifications.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendPartnerNotRespondedLetterToSoleApplicant() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setOffline(YesOrNo.YES);

        aosReminderNotifications.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            data,
            TEST_CASE_ID,
            data.getApplicant1(),
            aosOverdueDocumentPack.getDocumentPack(data, data.getApplicant1()),
            aosOverdueDocumentPack.getLetterId()
        );
    }

    @Test
    void shouldNotSendEmailToSoleRespondentWhenBasedOverseas() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant2().setEmail(TEST_USER_EMAIL);
        data.getApplicant2().setAddress(AddressGlobalUK.builder()
            .addressLine1("223b")
            .addressLine2("Baker Street")
            .postTown("Tampa")
            .county("Florida")
            .country("United States")
            .build());
        aosReminderNotifications.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendPartnerNotRespondedToSoleApplicantEmailInWelshWhenLanguagePreferenceIsWelsh() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        aosReminderNotifications.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }
}
