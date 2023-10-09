package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLICATION_WITHDRAWN;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
public class ApplicationWithdrawnNotificationTest {
    private static final String IS_RESPONDENT = "isRespondent";
    private static final String RESPONDENT_PARTNER = "respondentPartner";

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationWithdrawnNotification applicationWithdrawnNotification;

    @Test
    void shouldSendEmailToSoleApplicant1WithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleApplicant1WithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(dissolutionTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToSoleApplicant1IfLanguagePreferenceIsWelsh() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, YES),
                hasEntry(RESPONDENT_PARTNER, "husband")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
        verify(commonContent).getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, YES),
                hasEntry(RESPONDENT_PARTNER, "husband")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
        verify(commonContent).getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendWelshEmailToApplicant2IfLanguagePreferenceIsWelsh() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("gŵr");

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, YES),
                hasEntry(RESPONDENT_PARTNER, "gŵr")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
        verify(commonContent).getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference());
    }

    @Test
    void shouldNotSendEmailApplicant2IfEmailIsNull() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail(null);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailApplicant2IfEmailIsEmptyString() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail("");

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailToSoleRespondentIfNotIssued() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(null);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToJointApplicant2IfNotIssued() {
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(null);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService, times(1)).sendEmail(
            data.getApplicant2EmailAddress(),
            CITIZEN_APPLICATION_WITHDRAWN,
            divorceTemplateVars,
            data.getApplicant2().getLanguagePreference(),
            TEST_CASE_ID
        );
    }
}
