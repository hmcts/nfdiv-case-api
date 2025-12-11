package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.DISPENSE_SERVICE_APPLICATION_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.DISPENSE_SERVICE_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class DispenseServiceApplicationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private DispenseServiceApplicationSubmittedNotification notification;

    @Test
    void shouldSendAwaitingDocumentsNotificationIfSomeDocsWereNotUploaded() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
                .serviceApplicationDocsUploadedPreSubmission(YesOrNo.NO)
                .alternativeServiceFeeRequired(YesOrNo.NO)
                .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.serviceApplicationTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            DISPENSE_SERVICE_APPLICATION_AWAITING_DOCUMENTS,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendApplicationSubmittedNotificationIfAllDocsWereUploaded() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.serviceApplicationTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            DISPENSE_SERVICE_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldTriggerWelshNotifications() {
        CaseData data = validCaseDataForIssueApplication();

        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.serviceApplicationTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            DISPENSE_SERVICE_APPLICATION_SUBMITTED,
            templateVars,
            WELSH,
            TEST_CASE_ID
        );
    }
}
