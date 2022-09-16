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
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFinalOrderSolicitorsVars;

@ExtendWith(MockitoExtension.class)
class BothApplicantSolicitorsAppliedForFinalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BothApplicantSolicitorsAppliedForFinalOrderNotification notification;

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

        when(commonContent.basicTemplateVars(data, 1L)).thenReturn((getFinalOrderSolicitorsVars(data, data.getApplicant1())));

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant1().getSolicitor().getName())
                )),
                eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, 1L);
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

        when(commonContent.basicTemplateVars(data, 1L)).thenReturn(getFinalOrderSolicitorsVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, 1L);

        verify(notificationService).sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
                argThat(allOf(
                        hasEntry(IS_FINAL_ORDER, YES),
                        hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName())
                )),
                eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(data, 1L);
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
                .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
                .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
                .build());

        notification.sendToApplicant1Solicitor(data, 1L);

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
}