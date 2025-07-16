package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMediumType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.MADE_PAYMENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.USED_HELP_WITH_FEES;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.ALTERNATIVE_SERVICE_APPLICATION_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class AlternativeServiceApplicationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private AlternativeServiceApplicationSubmittedNotification notification;

    @BeforeEach
    void setEnvironment() {
        ReflectionTestUtils.setField(notification, "applicationResponseOffsetDays", 28L);
    }

    @Test
    void shouldSendAwaitingDocumentsNotificationIfSomeDocsWereNotUploaded() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
                .serviceApplicationDocsUploadedPreSubmission(YesOrNo.NO)
                .alternativeServiceFeeRequired(YesOrNo.NO)
                .alternativeServiceMediumSelected(Set.of(AlternativeServiceMediumType.EMAIL))
                .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(ALTERNATIVE_SERVICE_APPLICATION_AWAITING_DOCUMENTS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(CommonContent.FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName()),
                hasEntry(MADE_PAYMENT, NO),
                hasEntry(USED_HELP_WITH_FEES, YES),
                hasEntry(SUBMISSION_RESPONSE_DATE, ""),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.MULTIPLE_WAYS_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.DIFFERENT_WAY_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.OPTIONAL_PARTNER_LABEL, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicationSubmittedNotificationIfAllDocsWereUploaded() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .alternativeServiceMediumSelected(Set.of(AlternativeServiceMediumType.EMAIL))
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(CommonContent.FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName()),
                hasEntry(MADE_PAYMENT, YES),
                hasEntry(USED_HELP_WITH_FEES, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, "29 January 2020"),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.MULTIPLE_WAYS_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.DIFFERENT_WAY_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.OPTIONAL_PARTNER_LABEL, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldTriggerWelshNotifications() {
        CaseData data = validCaseDataForIssueApplication();

        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .alternativeServiceMediumSelected(Set.of(AlternativeServiceMediumType.EMAIL))
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(CommonContent.FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName()),
                hasEntry(MADE_PAYMENT, YES),
                hasEntry(USED_HELP_WITH_FEES, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, "29 Ionawr 2020")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicationSubmittedNotificationWithCorrectTemplateContentWhenMultipleWaysSelected() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .alternativeServiceMediumSelected(Set.of(AlternativeServiceMediumType.EMAIL, AlternativeServiceMediumType.TEXT))
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(CommonContent.FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName()),
                hasEntry(MADE_PAYMENT, YES),
                hasEntry(USED_HELP_WITH_FEES, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, "29 January 2020"),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.MULTIPLE_WAYS_SELECTED, YES),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.DIFFERENT_WAY_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.OPTIONAL_PARTNER_LABEL, "")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicationSubmittedNotificationWithCorrectTemplateContentWhenOneWaySelected() {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .alternativeServiceMediumSelected(Set.of(AlternativeServiceMediumType.TEXT))
            .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(CommonContent.FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName()),
                hasEntry(MADE_PAYMENT, YES),
                hasEntry(USED_HELP_WITH_FEES, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, "29 January 2020"),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.MULTIPLE_WAYS_SELECTED, NO),
                hasEntry(AlternativeServiceApplicationSubmittedNotification.DIFFERENT_WAY_SELECTED, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }
}
