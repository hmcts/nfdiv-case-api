package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;

@ExtendWith(MockitoExtension.class)
class SolicitorServiceNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SolicitorServiceNotification solicitorServiceNotification;

    @Test
    void shouldSendPersonalServiceNotificationToApplicantSolicitor() {

        final CaseData caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .build();

        when(commonContent.basicTemplateVars(caseData, TEST_CASE_ID)).thenReturn(commonTemplateVars());

        solicitorServiceNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            APPLICANT_SOLICITOR_SERVICE,
            personalServiceTemplateVars(),
            ENGLISH
        );

        verifyNoMoreInteractions(notificationService);
    }

    private Map<String, String> personalServiceTemplateVars() {

        final Map<String, String> templateVars = commonTemplateVars();
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
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
