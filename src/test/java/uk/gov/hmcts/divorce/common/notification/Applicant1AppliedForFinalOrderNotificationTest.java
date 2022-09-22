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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

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
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

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
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
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
        data.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().minusDays(30)).build());

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
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndApplicant2HasNotAppliedForFinalOrderYet() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
                .name("App2 Sol")
                .reference("12344")
                .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        when(commonContent.solicitorTemplateVars(data, 1L, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
            any(),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, 1L, data.getApplicant2());
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfJointApplicationAndApplicant2HasAlreadyAppliedForFinalOrder() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
                .applicant2AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        notification.sendToApplicant2Solicitor(data, 1L);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2Solicitor(data, 1L);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
    }
}
