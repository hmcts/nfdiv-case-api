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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT1_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class Applicant1SwitchToSoleAfterIntentionFONotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private Applicant1SwitchToSoleAfterIntentionFONotification notification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfJointApplicationAndApplicant1IntendedToSwitchToSoleFO() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("App1 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.solicitorTemplateVars(data, 1L, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));

        notification.sendToApplicant1Solicitor(data, 1L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT1_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, 1L, data.getApplicant1());
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
    void shouldNotSendApplicant1SolicitorNotificationIfApplicant1DoesntIntendToSwitchToSole() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(emptySet())
            .build());

        notification.sendToApplicant1Solicitor(data, 1L);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
