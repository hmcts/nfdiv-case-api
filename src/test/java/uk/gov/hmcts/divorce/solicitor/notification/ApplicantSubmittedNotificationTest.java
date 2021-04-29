package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.notification.ApplicantSubmittedNotification;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;

@ExtendWith(MockitoExtension.class)
class ApplicantSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicantSubmittedNotification applicationSubmittedNotification;

    @Test
    void shouldNotifyApplicantByApplicationSubmittedEmail() {

        final String petitionerEmail = "test@somewher.com";
        final Map<String, String> templateVars = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .petitionerEmail(petitionerEmail)
            .languagePreferenceWelsh(NO)
            .build();

        when(commonContent.templateVarsFor(caseData)).thenReturn(templateVars);

        applicationSubmittedNotification.send(caseData, 1234567890123456L);

        assertThat(templateVars.get(APPLICATION_REFERENCE), is("1234-5678-9012-3456"));

        verify(notificationService).sendEmail(
            petitionerEmail,
            SOL_APPLICANT_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH);
    }

    @Test
    void shouldNotifyApplicantByAmendedApplicationSubmittedEmail() {

        final String petitionerEmail = "test@somewher.com";
        final Map<String, String> templateVars = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .petitionerEmail(petitionerEmail)
            .languagePreferenceWelsh(NO)
            .previousCaseId(new CaseLink("Ref"))
            .build();

        when(commonContent.templateVarsFor(caseData)).thenReturn(templateVars);

        applicationSubmittedNotification.send(caseData, 1234567890123456L);

        assertThat(templateVars.get(APPLICATION_REFERENCE), is("1234-5678-9012-3456"));

        verify(notificationService).sendEmail(
            petitionerEmail,
            SOL_APPLICANT_AMENDED_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH);
    }

    @Test
    void shouldNotNotifyApplicantIfNoEmailSet() {

        final CaseData caseData = CaseData.builder()
            .languagePreferenceWelsh(NO)
            .build();

        applicationSubmittedNotification.send(caseData, 1L);

        verifyNoInteractions(notificationService);
    }
}