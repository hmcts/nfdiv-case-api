package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class AwaitingFinalOrderReminderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AwaitingFinalOrderReminderNotification awaitingFinalOrderReminderNotification;

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant1IfNotRepresentedAndSole() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant1IfNotRepresentedAndSoleInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant1IfNotRepresentedAndJoint() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant1IfNotRepresentedAndJointInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotSendAwaitingFinalOrderReminderEmailToApplicant2IfNotRepresentedAndSole() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        awaitingFinalOrderReminderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant2IfNotRepresentedAndJoint() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailToApplicant2IfNotRepresentedAndJointInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAwaitingFinalOrderReminderEmailWithCorrectTemplateVars() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.YES),
                hasEntry(DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS, "21 March 2022")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }
}
