package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.util.Objects;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class Applicant1AppliedForFinalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private Applicant1AppliedForFinalOrderNotification notification;

    @Mock
    private Clock clock;

    @Test
    void shouldSendApplicant1NotificationIfSoleApplication() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    public void verifyApplicant1TemplateVarsWhenFinalOrderEligible() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(WILL_BE_CHECKED_WITHIN_2_DAYS, CommonContent.YES),
                hasEntry(WILL_BE_CHECKED_WITHIN_14_DAYS, CommonContent.NO),
                hasEntry(NOW_PLUS_14_DAYS, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    public void verifyApplicant1TemplateVarsWhenFinalOrderNotEligible() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().minusDays(30)).build());

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(WILL_BE_CHECKED_WITHIN_2_DAYS, CommonContent.NO),
                hasEntry(WILL_BE_CHECKED_WITHIN_14_DAYS, CommonContent.YES),
                hasEntry(NOW_PLUS_14_DAYS, getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndIsRepresented() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL).build());

        when(commonContent.basicTemplateVars(data, 1L)).thenReturn(getBasicTemplateVars());

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                any(),
                eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, 1L);
    }

    @Test
    public void verifyApplicant1SolicitorTemplateVars() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .name(SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .build());

        when(commonContent.basicTemplateVars(data, 1L)).thenReturn(getBasicTemplateVars());

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, CommonContent.YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant1().getSolicitor().getName())
                )),
                eq(ENGLISH)
        );
        verify(commonContent).basicTemplateVars(data, 1L);
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
    }
}
