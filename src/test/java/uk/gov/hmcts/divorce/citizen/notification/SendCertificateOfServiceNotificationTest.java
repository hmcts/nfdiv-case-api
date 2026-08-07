package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CERTIFICATE_OF_SERVICE_EVIDENCE_NOTIFICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class SendCertificateOfServiceNotificationTest {

    private static final long CASE_ID = 123456789L;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SendCertificateOfServiceNotification sendCertificateOfServiceNotification;

    @Test
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseData() {
        CaseData caseData = validApplicant2CaseData();

        sendCertificateOfServiceNotification.notifyApplicant(caseData, CASE_ID);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CERTIFICATE_OF_SERVICE_EVIDENCE_NOTIFICATION),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }
}
