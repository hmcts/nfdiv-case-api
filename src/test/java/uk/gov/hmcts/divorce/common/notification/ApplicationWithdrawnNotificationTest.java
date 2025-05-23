package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class ApplicationWithdrawnNotificationTest {
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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        caseDetails.setData(data);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        caseDetails.setData(data);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(dissolutionTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseDetails.setData(data);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant1(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        caseDetails.setData(data);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        caseDetails.setData(data);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        caseDetails.setData(data);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);
        caseDetails.setData(data);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseDetails.setData(data);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);
        when(commonContent.getPartner(data, data.getApplicant1(), data.getApplicant2().getLanguagePreference()))
            .thenReturn("gŵr");

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

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
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail(null);
        caseDetails.setData(data);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailApplicant2IfEmailIsEmptyString() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail("");
        caseDetails.setData(data);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailToApplicant2IfJointAndApp2HasNotBeenInvited() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_EMAIL);
        data.setApplicationType(JOINT_APPLICATION);
        caseDetails.setData(data);
        caseDetails.setState(State.Draft);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailToSoleRespondentIfNotIssued() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(null);
        caseDetails.setData(data);

        applicationWithdrawnNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToJointApplicant2IfNotIssued() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(null);
        caseDetails.setData(data);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicationWithdrawnNotification.sendToApplicant2(caseDetails);

        verify(notificationService, times(1)).sendEmail(
            data.getApplicant2EmailAddress(),
            CITIZEN_APPLICATION_WITHDRAWN,
            divorceTemplateVars,
            data.getApplicant2().getLanguagePreference(),
            TEST_CASE_ID
        );
    }
}
