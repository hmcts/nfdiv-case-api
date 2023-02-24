package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.print.FinalOrderGrantedPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANTS_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_APPLICANT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;

@ExtendWith(MockitoExtension.class)
public class FinalOrderGrantedNotificationTest {

    private static final long TEST_CASE_ID = 1234567890123456L;

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinalOrderGrantedPrinter printer;

    @InjectMocks
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Test
    void shouldSendFinalOrderGrantedEmailToApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        final Applicant applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        finalOrderGrantedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANTS_FINAL_ORDER_GRANTED),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendSwitchedToSoleFinalOrderGrantedEmailToApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        final Applicant applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        caseData.getFinalOrder().setFinalOrderSwitchedToSole(YesOrNo.YES);

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        finalOrderGrantedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_APPLICANT),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendFinalOrderGrantedEmailToApplicant1Solicitor() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .email(TEST_USER_EMAIL)
                .reference("App1 Sol Ref")
                .build()
        );

        final Applicant applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");
        templateContent.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateContent.put(RESPONDENT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateContent.put(IS_SOLE, YES);
        templateContent.put(IS_JOINT, NO);
        templateContent.put(SOLICITOR_NAME, "App1 Sol");
        templateContent.put(DATE_OF_ISSUE, LocalDate.of(2021, 4, 28).format(DATE_TIME_FORMATTER));
        templateContent.put(SOLICITOR_REFERENCE, "App1 Sol Ref");
        templateContent.put(SIGN_IN_URL, "signin_url");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("signin_url");

        finalOrderGrantedNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLICITOR_FINAL_ORDER_GRANTED),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendFinalOrderGrantedEmailToApplicant2() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        final Applicant applicant2 = getApplicant();
        applicant2.setSolicitorRepresented(YesOrNo.NO);
        caseData.setApplicant2(applicant2);

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        finalOrderGrantedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANTS_FINAL_ORDER_GRANTED),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendSwitchedToSoleFinalOrderGrantedEmailToApplicant2() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        final Applicant applicant2 = getApplicant();
        applicant2.setSolicitorRepresented(YesOrNo.NO);
        caseData.setApplicant2(applicant2);

        caseData.getFinalOrder().setFinalOrderSwitchedToSole(YesOrNo.YES);

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        finalOrderGrantedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_RESPONDENT),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendFinalOrderGrantedEmailToApplicant2Solicitor() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));

        final Applicant applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .email(TEST_USER_EMAIL)
                .build()
        );

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateContent.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateContent.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(PARTNER, "partner");
        templateContent.put(COURT_EMAIL, "courtEmail");
        templateContent.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateContent.put(RESPONDENT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateContent.put(IS_SOLE, NO);
        templateContent.put(IS_JOINT, YES);
        templateContent.put(SOLICITOR_NAME, "App2 Sol");
        templateContent.put(DATE_OF_ISSUE, LocalDate.of(2021, 4, 28).format(DATE_TIME_FORMATTER));
        templateContent.put(SOLICITOR_REFERENCE, "not provided");
        templateContent.put(SIGN_IN_URL, "signin_url");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("signin_url");

        finalOrderGrantedNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLICITOR_FINAL_ORDER_GRANTED),
            eq(templateContent),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldPrintLettersForOfflineApplicant1() {
        final CaseData caseData = caseData();

        finalOrderGrantedNotification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(printer).print(caseData, TEST_CASE_ID, FINAL_ORDER_GRANTED_COVER_LETTER_APP_1);
    }

    @Test
    void shouldPrintLettersForOfflineApplicant2() {
        final CaseData caseData = caseData();

        finalOrderGrantedNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(printer).print(caseData, TEST_CASE_ID, FINAL_ORDER_GRANTED_COVER_LETTER_APP_2);
    }
}
