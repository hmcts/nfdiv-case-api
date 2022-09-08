package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
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
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

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
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
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
    public void verifyApplicant2TemplateVars() {
        setupMocks(clock);
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1())).thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_APPLIED_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(WILL_BE_CHECKED_WITHIN_2_DAYS, CommonContent.NO),
                hasEntry(WILL_BE_CHECKED_WITHIN_14_DAYS, CommonContent.YES),
                hasEntry(NOW_PLUS_14_DAYS, getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1());
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
    }
}
