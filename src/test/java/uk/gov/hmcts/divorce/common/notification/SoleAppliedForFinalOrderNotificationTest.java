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
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.notification.SoleAppliedForFinalOrderNotification.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.SoleAppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.SoleAppliedForFinalOrderNotification.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class SoleAppliedForFinalOrderNotificationTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SoleAppliedForFinalOrderNotification notification;

    @Mock
    private Clock clock;

    @Test
    void shouldSendApplicant1NotificationIfTheyAreCurrentUser() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);

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
    void shouldSendApplicant2NotificationIfTheyAreCurrentUser() {
        setupMocks(clock);
        CaseData data = validApplicant2CaseData();
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(false);

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
    public void verifyApplicant1TemplateVarsWhenFinalOrderEligible() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);

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
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().minusDays(30)).build()
        );

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);

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
    public void verifyApplicant2TemplateVars() {
        setupMocks(clock);
        CaseData data = validApplicant2CaseData();
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build()
        );
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(false);

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
        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);
    }
}
