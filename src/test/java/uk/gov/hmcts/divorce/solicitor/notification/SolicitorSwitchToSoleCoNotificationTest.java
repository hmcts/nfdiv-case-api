package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class SolicitorSwitchToSoleCoNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SolicitorSwitchToSoleCoNotification solicitorSwitchToSoleCoNotification;

    @Test
    void shouldSendNotificationToApplicant1Solicitor() {

        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor
            .builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());

        final Map<String, String> templateVars = new HashMap<>();
        when(commonContent.solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1()))
            .thenReturn(templateVars);

        solicitorSwitchToSoleCoNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICANT_NAME, caseData.getApplicant1().getFullName())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2Solicitor() {

        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor
            .builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());

        final Map<String, String> templateVars = new HashMap<>();
        when(commonContent.solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2()))
            .thenReturn(templateVars);

        solicitorSwitchToSoleCoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICANT_NAME, caseData.getApplicant2().getFullName())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicant2() {

        final CaseData caseData = validJointApplicant1CaseData();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateVars);

        solicitorSwitchToSoleCoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2Welsh() {

        final CaseData caseData = validJointApplicant1CaseData();
        final Map<String, String> templateVars = new HashMap<>();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateVars);

        solicitorSwitchToSoleCoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            eq(templateVars),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }
}
