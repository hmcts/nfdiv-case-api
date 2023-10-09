package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification.APPLICANT_2_COMMENTS;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification.PARTNER_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification.REQUESTED_CHANGES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_APPLICANT2_REQUESTED_CHANGES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class Applicant2RequestChangesNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private Applicant2RequestChangesNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DIVORCE_URL, "sign in divorce link"));

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DISSOLUTION_URL, "sign in dissolution link"));

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DIVORCE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithDivorceContent() {
        CaseData data = validApplicant1CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        data.getApplicant1().setSolicitor(Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build());
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getBasicTemplateVars());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DIVORCE_URL, "sign in divorce link"));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_APPLICANT2_REQUESTED_CHANGES),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(PARTNER_IS_REPRESENTED, "Yes"),
                hasEntry(REQUESTED_CHANGES, "Not correct!"),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithDissolutionContent() {
        CaseData data = validApplicant1CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplicant1().setSolicitor(Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build());
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getBasicTemplateVars());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DIVORCE_URL, "sign in divorce link"));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_APPLICANT2_REQUESTED_CHANGES),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(PARTNER_IS_REPRESENTED, "No"),
                hasEntry(REQUESTED_CHANGES, "Not correct!"),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DIVORCE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentWhenLangPrefIsWelsh() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(
                anyOf(
                    hasEntry(PARTNER, "gŵr")
                )
            ),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentWhenLangPrefIsWelsh() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        templateVars.put(PARTNER, "gwraig");

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(
                anyOf(
                    hasEntry(PARTNER, "gwraig")
                )
            ),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WhenApplicant1RepresentedWithDivorceContent() {
        CaseData data = caseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WhenApplicant1RepresentedWithDissolutionContent() {
        CaseData data = caseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setCaseInvite(new CaseInvite(TEST_USER_EMAIL, null, null));

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }
}
