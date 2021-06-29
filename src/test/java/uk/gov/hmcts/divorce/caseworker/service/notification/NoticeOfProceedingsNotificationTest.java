package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.common.model.Gender.MALE;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CASE_ID;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class NoticeOfProceedingsNotificationTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Test
    void shouldSendNotificationToApplicantSolicitorIfApplicantIsRepresentedBySolicitor() {

        final CaseData caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .applicant2(respondent())
            .build();

        noticeOfProceedingsNotification.sendToApplicantOrSolicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
            solicitorTemplateVars(),
            ENGLISH
        );
    }

    @Test
    void shouldSendNotificationToApplicantIfApplicantIsNotRepresentedBySolicitor() {

        final Applicant applicant = getApplicant();
        applicant.setLanguagePreferenceWelsh(YES);

        final CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .applicant2(respondent())
            .build();

        noticeOfProceedingsNotification.sendToApplicantOrSolicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            APPLICANT_NOTICE_OF_PROCEEDINGS,
            commonTemplateVars(),
            WELSH
        );
    }

    private Applicant applicantRepresentedBySolicitor() {
        final Applicant applicant = getApplicant(FEMALE);
        applicant.setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        return applicant;
    }

    private Applicant respondent() {
        return Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();
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
