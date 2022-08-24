package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.print.CertificateOfEntitlementPrinter;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING_MINUS_SEVEN_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseWithCourtHearing;

@ExtendWith(SpringExtension.class)
class EntitlementGrantedConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private CertificateOfEntitlementPrinter certificateOfEntitlementPrinter;

    @InjectMocks
    private EntitlementGrantedConditionalOrderNotification entitlementGrantedConditionalOrderNotification;

    @Test
    void shouldSendEmailToApplicant1WithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021"),
                hasEntry(ISSUE_DATE, "8 August 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(IS_SOLE, "yes"),
                hasEntry(IS_JOINT, "no"),
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021"),
                hasEntry(ISSUE_DATE, "8 August 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(IS_SOLE, "no"),
                hasEntry(IS_JOINT, "yes"),
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021"),
                hasEntry(ISSUE_DATE, "8 August 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setOffline(YesOrNo.NO);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Solicitor")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App1 Sol Ref")
                .build()
        );
        data.getApplicant1().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(1234567890123456L)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        entitlementGrantedConditionalOrderNotification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(IS_SOLE, "yes"),
                hasEntry(IS_JOINT, "no"),
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021"),
                hasEntry(ISSUE_DATE, "8 August 2021"),
                hasEntry(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
                hasEntry(RESPONDENT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
                hasEntry(SOLICITOR_NAME, "App1 Solicitor"),
                hasEntry(SOLICITOR_REFERENCE, "App1 Sol Ref"),
                hasEntry(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Solicitor")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App2 Sol Ref")
                .build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(1234567890123456L)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        entitlementGrantedConditionalOrderNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(IS_SOLE, "no"),
                hasEntry(IS_JOINT, "yes"),
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021"),
                hasEntry(ISSUE_DATE, "8 August 2021"),
                hasEntry(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
                hasEntry(RESPONDENT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
                hasEntry(SOLICITOR_NAME, "App2 Solicitor"),
                hasEntry(SOLICITOR_REFERENCE, "App2 Sol Ref"),
                hasEntry(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToSolicitorWhenNoReferenceProvided() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Solicitor")
                .email(TEST_SOLICITOR_EMAIL)
                .build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2021, 8, 8));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(SOLICITOR_REFERENCE, "not provided")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendLetterToOfflineApplicant1IfNotBeenSentAlready() {
        CaseData data = validCaseWithCourtHearing();

        entitlementGrantedConditionalOrderNotification.sendToApplicant1Offline(data, 1234567890123456L);

        verify(certificateOfEntitlementPrinter).sendLetter(data, 1234567890123456L, data.getApplicant1());
        assertThat(data.getConditionalOrder().hasOfflineCertificateOfEntitlementBeenSentToApplicant1()).isTrue();
    }

    @Test
    void shouldNotSendLetterToOfflineApplicant1IfHasAlreadyBeenSent() {
        CaseData data = validCaseWithCourtHearing();
        data.getConditionalOrder().setOfflineCertificateOfEntitlementDocumentSentToApplicant1(YesOrNo.YES);

        entitlementGrantedConditionalOrderNotification.sendToApplicant1Offline(data, 1234567890123456L);

        verifyNoInteractions(certificateOfEntitlementPrinter);
    }

    @Test
    void shouldSendLetterToOfflineApplicant2IfNotBeenSentAlready() {
        CaseData data = validCaseWithCourtHearing();

        entitlementGrantedConditionalOrderNotification.sendToApplicant2Offline(data, 1234567890123456L);

        verify(certificateOfEntitlementPrinter).sendLetter(data, 1234567890123456L, data.getApplicant2());
        assertThat(data.getConditionalOrder().hasOfflineCertificateOfEntitlementBeenSentToApplicant2()).isTrue();
    }

    @Test
    void shouldNotSendLetterToOfflineApplicant2IfHasAlreadyBeenSent() {
        CaseData data = validCaseWithCourtHearing();
        data.getConditionalOrder().setOfflineCertificateOfEntitlementDocumentSentToApplicant2(YesOrNo.YES);

        entitlementGrantedConditionalOrderNotification.sendToApplicant2Offline(data, 1234567890123456L);

        verifyNoInteractions(certificateOfEntitlementPrinter);
    }
}
