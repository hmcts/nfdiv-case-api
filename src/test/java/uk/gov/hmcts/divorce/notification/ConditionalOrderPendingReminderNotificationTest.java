package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderQuestions;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPendingReminderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Test
    void shouldSendNotificationToApplicant1WhenSubmittedDateIsNotSet() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant1(),
                caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        conditionalOrderPendingReminderNotification.sendToApplicant1(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant1WhenSubmittedDateIsSet() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        conditionalOrderPendingReminderNotification.sendToApplicant1(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToApplicant1SolicitorWhenSubmittedDateIsNotSet() {
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var caseData = CaseData.builder().applicant1(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        when(commonContent.basicTemplateVars(caseData, 1234567890123456L)).thenReturn(getBasicTemplateVars());

        conditionalOrderPendingReminderNotification.sendToApplicant1Solicitor(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_AWAITING_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant1SolicitorWhenSubmittedDateIsSet() {
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var caseData = CaseData.builder().applicant1(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        conditionalOrderPendingReminderNotification.sendToApplicant1Solicitor(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToApplicant2WhenSubmittedDateIsSet() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant2(),
                caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant2WhenSubmittedDateIsNotSet() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNotificationToApplicant2WhenApplicant2EmailNotSet() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(null);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNotificationToApplicant2SolicitorWhenSubmittedDateIsNotSet() {
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var caseData = CaseData.builder().applicant2(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        conditionalOrderPendingReminderNotification.sendToApplicant2Solicitor(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToApplicant2SolicitorWhenSubmittedDateIsSet() {
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var caseData = CaseData.builder().applicant2(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        when(commonContent.basicTemplateVars(caseData, 1234567890123456L)).thenReturn(getBasicTemplateVars());

        conditionalOrderPendingReminderNotification.sendToApplicant2Solicitor(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_AWAITING_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456")
            )),
            eq(ENGLISH)
        );
    }
}
