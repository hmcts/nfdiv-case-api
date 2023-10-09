package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant2JointFinalOrderOverdueNotificationTest {

    private static final String APPLICANT_1_DELAY_CONTENT = "They applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\nForgot";

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @InjectMocks
    private Applicant2RemindAwaitingJointFinalOrderNotification notification;

    @Test
    void shouldSendApplicant2NotificationWhenJointFinalOrderIsOverdueFromApplicant2() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_USER_EMAIL);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1(), true))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verifyNoMoreInteractions(finalOrderNotificationCommonContent);
    }

    @Test
    void shouldSendApplicant2NotificationWhenJointFinalOrderIsOverdueFromApplicant1() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_USER_EMAIL);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
                .applicant1FinalOrderLateExplanation("Forgot")
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1(), true))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry("delayReasonIfOverdue", APPLICANT_1_DELAY_CONTENT)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verifyNoMoreInteractions(finalOrderNotificationCommonContent);
    }

    @Test
    void shouldNotSendApplicant2NotificationWhenSoleCase() {
        CaseData data = validJointApplicant1CaseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(finalOrderNotificationCommonContent);
    }
}
