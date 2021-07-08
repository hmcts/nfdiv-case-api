package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseDataMap;

@ExtendWith(MockitoExtension.class)
public class ApplicationSentForReviewApplicant2NotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationSentForReviewApplicant2Notification notification;

    @Test
    void shouldSendEmailToApplicant2WhileInAwaitingApplicant2ResponseState() {
        CaseData data = validJointApplicant1CaseDataMap();
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant1())).thenReturn("husband");
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(PARTNER, "husband"),
                hasEntry(CREATE_ACCOUNT_LINK, APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsFor(data);
        verify(commonContent).getTheirPartner(data, data.getApplicant1());
    }

    @Test
    void shouldSetTheAppropriateFieldsForDissolutionCases() {
        CaseData data = validJointApplicant1CaseDataMap();
        data.setDueDate(LOCAL_DATE);
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant1())).thenReturn("husband");
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(PARTNER, "husband"),
                hasEntry(CREATE_ACCOUNT_LINK, APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsFor(data);
        verify(commonContent).getTheirPartner(data, data.getApplicant1());
    }

}
