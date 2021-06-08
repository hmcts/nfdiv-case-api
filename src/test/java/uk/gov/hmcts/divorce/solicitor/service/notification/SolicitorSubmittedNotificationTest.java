package uk.gov.hmcts.divorce.solicitor.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Test
    void shouldNotifyApplicantSolicitorByApplicationSubmittedEmail() {

        final String applicant1SolicitorEmail = "test@somewher.com";
        final Map<String, String> templateVars = new HashMap<>();
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder().email(applicant1SolicitorEmail).build()
        );

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        when(commonContent.templateVarsFor(caseData)).thenReturn(templateVars);

        solicitorSubmittedNotification.send(caseData, 1234567890123456L);

        assertThat(templateVars.get(APPLICATION_REFERENCE), is("1234-5678-9012-3456"));

        verify(notificationService).sendEmail(
            applicant1SolicitorEmail,
            SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH);
    }

    @Test
    void shouldNotifyApplicantSolicitorByAmendedApplicationSubmittedEmail() {

        final String applicant1SolicitorEmail = "test@somewher.com";
        final Map<String, String> templateVars = new HashMap<>();
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder().email(applicant1SolicitorEmail).build()
        );

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .previousCaseId(new CaseLink("Ref"))
            .build();

        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        when(commonContent.templateVarsFor(caseData)).thenReturn(templateVars);

        solicitorSubmittedNotification.send(caseData, 1234567890123456L);

        assertThat(templateVars.get(APPLICATION_REFERENCE), is("1234-5678-9012-3456"));

        verify(notificationService).sendEmail(
            applicant1SolicitorEmail,
            SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH);
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
