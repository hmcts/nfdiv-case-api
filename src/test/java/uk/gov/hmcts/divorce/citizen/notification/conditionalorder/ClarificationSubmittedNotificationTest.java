package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_SUBMISSION_DATE_PLUS_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_PARTNER_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.WELSH_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class ClarificationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ClarificationSubmittedNotification clarificationSubmittedNotification;

    @Test
    void shouldSendEmailToSoleApplicant1WithDivorceContent() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(COURT_EMAIL, "courtEmail"),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailInWelshToSoleApplicant1WithDivorceContent() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WithDivorceContentIfTheySubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(COURT_EMAIL, "courtEmail"),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailInWelshToJointApplicant1WithDivorceContentIfTheySubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WithDivorceContentIfTheirPartnerSubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(COURT_EMAIL, "courtEmail"),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailInWelshToJointApplicant1WithDivorceContentIfTheirPartnerSubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }


    @Test
    void shouldSendEmailToJointApplicant2WithDivorceContentIfTheySubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(COURT_EMAIL, "courtEmail"),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailInWelshToJointApplicant2WithDivorceContentIfTheySubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithDivorceContentIfTheirPartnerSubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(COURT_EMAIL, "courtEmail"),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailInWelshToJointApplicant2WithDivorceContentIfTheirPartnerSubmittedClarification() {

        setMockClock(clock);

        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        clarificationSubmittedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent)
            .mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldNotSendEmailToApplicant2IfSoleCase() {

        CaseData caseData = new CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);

        clarificationSubmittedNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
