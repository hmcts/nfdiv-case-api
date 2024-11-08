package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REINVITE_CITIZEN_TO_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;


@ExtendWith(MockitoExtension.class)
class InviteApplicantToCaseNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig config;

    @InjectMocks
    private InviteApplicantToCaseNotification notification;

    @Test
    void shouldSendCaseInviteToApplicant1() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(Gender.MALE));

        CaseInviteApp1 invite = CaseInviteApp1.builder()
            .applicant1InviteEmailAddress(data.getApplicant1().getEmail())
            .accessCode("12345678")
            .build();
        data.setCaseInviteApp1(invite);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.send(data, TEST_CASE_ID, true);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(REINVITE_CITIZEN_TO_CASE),
            argThat(allOf(
                hasEntry(ACCESS_CODE, "12345678"),
                hasEntry(CREATE_ACCOUNT_LINK, APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendCaseInviteToApplicant2() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(Gender.MALE));
        data.getApplicant2().setEmail(TEST_USER_EMAIL);

        CaseInvite invite = CaseInvite.builder()
            .applicant2InviteEmailAddress(data.getApplicant2().getEmail())
            .accessCode("12345678")
            .build();
        data.setCaseInvite(invite);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(config.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.send(data, TEST_CASE_ID, false);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(REINVITE_CITIZEN_TO_CASE),
            argThat(allOf(
                hasEntry(ACCESS_CODE, "12345678"),
                hasEntry(CREATE_ACCOUNT_LINK, APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
