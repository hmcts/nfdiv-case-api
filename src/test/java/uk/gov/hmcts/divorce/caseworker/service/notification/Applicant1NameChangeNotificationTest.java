package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;

import static uk.gov.hmcts.divorce.notification.CommonContent.*;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.*;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class Applicant1NameChangeNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailTemplatesConfig config;

    @InjectMocks
    private Applicant1NameChangeNotification applicant1NameChangeNotification;

    @Test
    void shouldSendNameChangedEmailToApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        final Applicant applicant1 = caseData.getApplicant1();

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, TEST_CASE_ID != null ? formatId(TEST_CASE_ID) : null);
        templateVars.put(FIRST_NAME, applicant1.getFirstName());
        templateVars.put(LAST_NAME, applicant1.getLastName());
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(caseData.isDivorce() ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));

        applicant1NameChangeNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_NAME_UPDATED),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
