package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.notification.interimapplications.GeneralApplicationRejectedNotification.IS_OTHER_D11_APP;
import static uk.gov.hmcts.divorce.citizen.notification.interimapplications.GeneralApplicationRejectedNotification.IS_SEARCH_GOV_RECORDS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_REJECTED_BY_CASEWORKER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class GeneralApplicationRejectedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private GeneralApplicationRejectedNotification notification;

    @Test
    void shouldSendNotificationToApplicant1() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        templateVars.put(IS_OTHER_D11_APP, NO);
        templateVars.put(IS_SEARCH_GOV_RECORDS, YES);

        notification.send(data, TEST_CASE_ID, true, true);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_APPLICATION_REJECTED_BY_CASEWORKER,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendNotificationToApplicant2() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant2().setEmail(TEST_SOLICITOR_EMAIL);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        templateVars.put(IS_OTHER_D11_APP, YES);
        templateVars.put(IS_SEARCH_GOV_RECORDS, NO);

        notification.send(data, TEST_CASE_ID, false, false);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            GENERAL_APPLICATION_REJECTED_BY_CASEWORKER,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }
}
