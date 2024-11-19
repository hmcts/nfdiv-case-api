package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
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
import static uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification.APPLICANT_1_OVERDUE_CONTENT;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification.APPLICANT_2_OVERDUE_CONTENT;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFinalOrderSolicitorsVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class FinalOrderRequestedNotificationTest {

    private static final String APPLICANT_1_CONTENT = "test_first_name test_middle_name test_last_name applied more than 12 "
        + "months after the conditional order was made and gave the following reason:\nForgot";
    private static final String APPLICANT_2_CONTENT = "John Smith applied more than 12 months after the "
        + "conditional order was made and gave the following reason:\nForgot";

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FinalOrderRequestedNotification notification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfJointApplicationAndRepresented() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setFinalOrder(FinalOrder.builder()
                .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
                .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        data.getApplicant1().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL).build());

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn((getFinalOrderSolicitorsVars(data, data.getApplicant1())));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant1().getSolicitor().getName())
                )),
                eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndRepresented() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.setFinalOrder(FinalOrder.builder()
                .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
                .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        data.getApplicant2().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL).build());

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getFinalOrderSolicitorsVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName())
                )),
                eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
                .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
                .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
                .build());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldSendApplicant1NotificationIfJointApplication() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IN_TIME, YES),
                hasEntry(IS_OVERDUE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2NotificationIfJointApplication() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IN_TIME, YES),
                hasEntry(IS_OVERDUE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant1SolicitorNotificationIfJointApplicationAndRepresentedAndOverdue() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
            .applicant1FinalOrderLateExplanation("Forgot").build());

        data.getApplicant1().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL).build());

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn((getFinalOrderSolicitorsVars(data, data.getApplicant1())));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            argThat(allOf(
                hasEntry(IS_FINAL_ORDER, YES),
                hasEntry(SOLICITOR_NAME, data.getApplicant1().getSolicitor().getName()),
                hasEntry(APPLICANT_1_OVERDUE_CONTENT, APPLICANT_1_CONTENT)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndRepresentedAndOverdue() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setFirstName("John");
        data.getApplicant2().setLastName("Smith");
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
            .applicant2FinalOrderLateExplanation("Forgot").build());

        data.getApplicant2().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL).build());

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn((getFinalOrderSolicitorsVars(data, data.getApplicant2())));

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            argThat(allOf(
                hasEntry(IS_FINAL_ORDER, YES),
                hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName()),
                hasEntry(APPLICANT_2_OVERDUE_CONTENT, APPLICANT_2_CONTENT)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant1NotificationIfJointApplicationAndRepresentedAndOverdue() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
            .applicant1FinalOrderLateExplanation("Forgot").build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICANT_1_OVERDUE_CONTENT, APPLICANT_1_CONTENT),
                hasEntry(IN_TIME, NO),
                hasEntry(IS_OVERDUE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2NotificationIfJointApplicationAndRepresentedAndOverdue() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant2().setFirstName("John");
        data.getApplicant2().setMiddleName(null);
        data.getApplicant2().setLastName("Smith");
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
            .applicant2FinalOrderLateExplanation("Forgot").build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICANT_2_OVERDUE_CONTENT, APPLICANT_2_CONTENT),
                hasEntry(IN_TIME, NO),
                hasEntry(IS_OVERDUE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }
}
