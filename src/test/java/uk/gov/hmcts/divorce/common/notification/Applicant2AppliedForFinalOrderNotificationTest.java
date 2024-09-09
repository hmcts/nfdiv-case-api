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
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_OR_FO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONSE_DUE_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class Applicant2AppliedForFinalOrderNotificationTest {

    private static final String APPLICANT_2_CONTENT = "They applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\nForgot";
    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @InjectMocks
    private Applicant2AppliedForFinalOrderNotification notification;

    @Test
    void shouldSendApplicant2NotificationIfSoleApplication() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant2NotificationInWelshIfSoleApplication() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    public void verifyApplicant2TemplateVars() {

        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        String nowPlusFourteenDays = getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER);

        when(finalOrderNotificationCommonContent.getNowPlus14Days(data.getApplicant2())).thenReturn(nowPlusFourteenDays);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(WILL_BE_CHECKED_WITHIN_2_DAYS, CommonContent.NO),
                hasEntry(WILL_BE_CHECKED_WITHIN_14_DAYS, CommonContent.YES),
                hasEntry(NOW_PLUS_14_DAYS, nowPlusFourteenDays)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant1SolicitorNotificationIfJointApplicationAndApplicant1HasNotAppliedForFinalOrderYet() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("App1 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationWhenJointApplicationAndApplicant2SolicitorHasAppliedForFinalOrder() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("App2 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderSubmitted(LocalDateTime.of(2022, 9, 10, 1, 0))
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(data.getApplicant2().getSolicitor().getEmail()),
            eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER),
            argThat(allOf(
                hasEntry(RESPONSE_DUE_DATE, "24 September 2022"),
                hasEntry(CO_OR_FO, "final")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2NotificationWhenJointApplicant2AppliedForFO() {

        CaseData data = validJointApplicant1CaseData();
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1(), false)).thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verifyNoMoreInteractions(finalOrderNotificationCommonContent);
    }

    @Test
    void shouldSendApplicant1NotificationWhenJointApplicant2AppliedForFO() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2(), false)).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

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
    void shouldSendApplicant1SolicitorNotificationWhenJointApplicant2AppliedForFOAndOverdue() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
                .isFinalOrderOverdue(YesOrNo.YES)
                .applicant2FinalOrderLateExplanation("Forgot")
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(commonContent.solicitorTemplateVars(
            data, TEST_CASE_ID, data.getApplicant1())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry("delayReason", APPLICANT_2_CONTENT)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verifyNoMoreInteractions(finalOrderNotificationCommonContent);
    }

    @Test
    void shouldSendApplicant1NotificationWhenJointApplicant2AppliedForFOAndIsOverdue() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .isFinalOrderOverdue(YesOrNo.YES)
            .applicant2FinalOrderLateExplanation("Forgot")
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2(), false)).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry("delayReasonIfOverdue", APPLICANT_2_CONTENT)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verifyNoMoreInteractions(finalOrderNotificationCommonContent);
    }
}
