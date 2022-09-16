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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_OR_FO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONSE_DUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFinalOrderSolicitorsVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class Applicant2AppliedForFinalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private Applicant2AppliedForFinalOrderNotification notification;

    @Mock
    private Clock clock;

    @Test
    void shouldSendApplicant2NotificationIfSoleApplication() {
        setupMocks(clock);
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrder(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant2NotificationIfJointApplication() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
                .applicant1AppliedForFinalOrder(YesOrNo.YES)
                .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        notification.sendToApplicant2(data, 1L);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    public void verifyApplicant2TemplateVars() {
        setupMocks(clock);
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2AppliedForFinalOrder(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(WILL_BE_CHECKED_WITHIN_2_DAYS, NO),
                hasEntry(WILL_BE_CHECKED_WITHIN_14_DAYS, YES),
                hasEntry(NOW_PLUS_14_DAYS, getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndIsRepresented() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL).build());
        data.getApplicant2().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL).build());
        data.setFinalOrder(FinalOrder.builder()
                        .applicant1AppliedForFinalOrder(YesOrNo.YES)
                        .applicant2AppliedForFinalOrder(YesOrNo.YES).build());

        notification.sendToApplicant2Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                any(),
                eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).solicitorsFinalOrderTemplateVars(data, 1L, data.getApplicant2());
    }

    @Test
    public void verifyApplicant2SolicitorTemplateVars() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        data.getApplicant2().setSolicitor(Solicitor.builder()
                .name(SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .build());

        data.setFinalOrder(FinalOrder.builder()
                .applicant1AppliedForFinalOrder(YesOrNo.YES)
                .applicant2AppliedForFinalOrder(YesOrNo.YES).build());

        when(commonContent.solicitorsFinalOrderTemplateVars(data, 1L, data.getApplicant2())).thenReturn(getFinalOrderSolicitorsVars(data,
                data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName())
                )),
                eq(data.getApplicant2().getLanguagePreference())
        );
        verify(commonContent).solicitorsFinalOrderTemplateVars(data, 1L, data.getApplicant2());
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
            .applicant2AppliedForFinalOrder(YesOrNo.YES)
            .build());

        when(commonContent.solicitorTemplateVars(data, 1L, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, 1L, data.getApplicant1());
    }

    @Test
    void shouldSendToApplicant2SolicitorNotificationIfJointApplicationAndApplicant2SolicitorHasAppliedForFinalOrder() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .name("App1 Sol")
                .reference("12344")
                .email(TEST_SOLICITOR_EMAIL)
                .build());

        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant1AppliedForFinalOrder(YesOrNo.YES)
            .build());

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                any(),
                eq(ENGLISH)
        );

        verify(commonContent).solicitorsFinalOrderTemplateVars(data, 1L, data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1Solicitor(data, 1L);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfJointApplicationAndApplicant2SolicitorHasNotAppliedForFinalOrder() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);

        notification.sendToApplicant2Solicitor(data, 1L);

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
            .applicant2AppliedForFinalOrder(YesOrNo.YES)
            .build());

        when(commonContent.solicitorTemplateVars(data, 1L, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, 1L);

        verify(notificationService).sendEmail(
            eq(data.getApplicant2().getSolicitor().getEmail()),
            eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER),
            argThat(allOf(
                hasEntry(RESPONSE_DUE_DATE, "24 September 2022"),
                hasEntry(CO_OR_FO, "final")
            )),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, 1L, data.getApplicant2());
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
    }
}
