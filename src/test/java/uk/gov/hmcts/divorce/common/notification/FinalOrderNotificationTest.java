package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDate;
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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderNotification.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderNotification.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderNotification.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class FinalOrderNotificationTest {

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
    private FinalOrderNotification notification;

    @Mock
    private Clock clock;

    @Test
    void shouldSendApplicant1NotificationIfTheyAreCurrentUser() {
        setupMocks(clock);
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
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
        data.setApplicationType(SOLE_APPLICATION);
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
        data.setApplicationType(SOLE_APPLICATION);
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
        data.setApplicationType(SOLE_APPLICATION);
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
        data.setApplicationType(SOLE_APPLICATION);
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

    @Test
    void shouldSendApplicant1SolicitorNotificationIfApplicantIsRepresentedAndIsJointApplication() {

        Long caseId = 1L;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                        .email(TEST_SOLICITOR_EMAIL)
                        .reference("ref")
                        .name(TEST_SOLICITOR_NAME)
                        .build())
                    .firstName(TEST_FIRST_NAME)
                    .lastName(TEST_LAST_NAME)
                    .build()
            )
            .application(Application.builder()
                .issueDate(LocalDate.now())
                .build())
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .build();

        when(commonContent.basicTemplateVars(caseData, caseId)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(caseId)).thenReturn(SIGN_IN_DIVORCE_TEST_URL);

        notification.sendToApplicant1Solicitor(caseData, caseId);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            any(),
            eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(caseData, caseId);
        verify(commonContent).getProfessionalUsersSignInUrl(caseId);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfApplicantIsRepresentedAndIsJointApplication() {

        Long caseId = 1L;
        CaseData caseData = CaseData.builder()
            .applicant2(
                Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                        .email(TEST_SOLICITOR_EMAIL)
                        .reference("ref")
                        .name(TEST_SOLICITOR_NAME)
                        .build())
                    .firstName(TEST_FIRST_NAME)
                    .lastName(TEST_LAST_NAME)
                    .build()
            )
            .application(Application.builder()
                .issueDate(LocalDate.now())
                .build())
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .build();

        when(commonContent.basicTemplateVars(caseData, caseId)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(caseId)).thenReturn(SIGN_IN_DIVORCE_TEST_URL);

        notification.sendToApplicant2Solicitor(caseData, caseId);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            any(),
            eq(ENGLISH)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(caseData, caseId);
        verify(commonContent).getProfessionalUsersSignInUrl(caseId);
    }
}
