package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification.CASE_ID;
import static uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification.SOLICITOR_ORGANISATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class NoticeOfProceedingsNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Test
    void shouldSendNotificationToApplicantSolicitorIfApplicantIsRepresentedBySolicitor() {

        final CaseData caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .applicant2(respondent())
            .build();

        when(commonContent.commonNotificationTemplateVars(caseData, TEST_CASE_ID)).thenReturn(commonTemplateVars());

        noticeOfProceedingsNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
            solicitorTemplateVars(),
            ENGLISH
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToApplicantIfApplicantIsNotRepresentedBySolicitor() {

        final Applicant applicant = getApplicant();
        applicant.setLanguagePreferenceWelsh(YES);

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .applicant2(respondent())
            .build();

        when(commonContent.commonNotificationTemplateVars(caseData, TEST_CASE_ID)).thenReturn(commonTemplateVars());

        noticeOfProceedingsNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            APPLICANT_NOTICE_OF_PROCEEDINGS,
            commonTemplateVars(),
            WELSH
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToRespondentSolicitorIfRespondentIsRepresented() {

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .applicant2(respondentWithDigitalSolicitor())
            .build();

        when(commonContent.commonNotificationTemplateVars(caseData, TEST_CASE_ID))
            .thenReturn(commonTemplateVars())
            .thenReturn(commonTemplateVars());

        noticeOfProceedingsNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
            respondentSolicitorTemplateVars(),
            ENGLISH
        );

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            APPLICANT_NOTICE_OF_PROCEEDINGS,
            commonTemplateVars(),
            ENGLISH
        );

        verifyNoMoreInteractions(notificationService);
    }

    private Map<String, String> respondentSolicitorTemplateVars() {
        final Map<String, String> templateVars = solicitorTemplateVars();

        templateVars.put(SOLICITOR_ORGANISATION, TEST_ORG_NAME);

        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars() {

        final Map<String, String> templateVars = commonTemplateVars();

        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(CASE_ID, TEST_CASE_ID.toString());

        return templateVars;
    }

    private Map<String, String> commonTemplateVars() {

        final Map<String, String> templateVars = new HashMap<>();

        templateVars.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));

        return templateVars;
    }
}
