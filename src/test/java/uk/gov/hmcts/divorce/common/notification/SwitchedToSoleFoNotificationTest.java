package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SwitchedToSoleFoNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SwitchedToSoleFoNotification notification;

    @Test
    void shouldSendNotificationToApplicant1() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendWelshNotificationToApplicant1IfLanguagePreferenceIsWelsh() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicant2() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendWelshNotificationToApplicant2IfLanguagePreferenceIsWelsh() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        notification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2());
    }
}
