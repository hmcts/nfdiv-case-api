package uk.gov.hmcts.divorce.solicitor.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Captor
    private ArgumentCaptor<Map<String,String>> templateVarsCaptor;

    @Test
    void shouldNotifyApplicantSolicitorByApplicationSubmittedEmail() {

        final String applicant1SolicitorEmail = "test@somewher.com";
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder().email(applicant1SolicitorEmail).build()
        );

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        solicitorSubmittedNotification.send(caseData, 1234567890123456L);


        verify(notificationService).sendEmail(
            eq(applicant1SolicitorEmail),
            eq(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED),
            templateVarsCaptor.capture(),
            eq(ENGLISH));

        Map<String,String> templateVars = templateVarsCaptor.getValue();
        assertThat(templateVars.get(APPLICATION_REFERENCE).equals("1234-5678-9012-3456"));
        assertThat(templateVars.get(FIRST_NAME).equals("test_first_name"));
        assertThat(templateVars.get(LAST_NAME).equals("test_last_name"));


    }

    @Test
    void shouldNotifyApplicantSolicitorByAmendedApplicationSubmittedEmail() {

        final String applicant1SolicitorEmail = "test@somewher.com";
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder().email(applicant1SolicitorEmail).build()
        );

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .previousCaseId(new CaseLink("Ref"))
            .build();

        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        solicitorSubmittedNotification.send(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(applicant1SolicitorEmail),
            eq(SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED),
            templateVarsCaptor.capture(),
            eq(ENGLISH));

        Map<String,String> templateVars = templateVarsCaptor.getValue();
        assertThat(templateVars.get(APPLICATION_REFERENCE).equals("1234-5678-9012-3456"));
        assertThat(templateVars.get(FIRST_NAME).equals("test_first_name"));
        assertThat(templateVars.get(LAST_NAME).equals("test_last_name"));

    }

    @Test
    void shouldNotNotifyApplicantSolicitorIfNoEmailSet() {

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .build();

        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        solicitorSubmittedNotification.send(caseData, 1L);

        verifyNoInteractions(notificationService);
    }
}
