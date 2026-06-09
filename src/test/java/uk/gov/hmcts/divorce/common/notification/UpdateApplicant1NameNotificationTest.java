package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NAME_UPDATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;


@ExtendWith(MockitoExtension.class)
class UpdateApplicant1NameNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private UpdateApplicant1NameNotification notification;

    @Test
    void shouldSendEmailToApplicant1() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().email(TEST_USER_EMAIL).build())
            .applicant2(Applicant.builder().build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_NAME_UPDATED),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEnglishEmailToApplicant1Solicitor() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .solicitor(Solicitor.builder()
                    .email(TEST_SOLICITOR_EMAIL)
                    .reference(TEST_REFERENCE)
                    .build())
                .build())
            .applicant2(Applicant.builder().solicitorRepresented(NO).build())
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = new HashMap<>();

        notification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_NAME_UPDATED),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendWelshEmailToApplicant1() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().email(TEST_USER_EMAIL).languagePreferenceWelsh(YES).build())
            .applicant2(Applicant.builder().build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_NAME_UPDATED),
            eq(templateVars),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }
}
