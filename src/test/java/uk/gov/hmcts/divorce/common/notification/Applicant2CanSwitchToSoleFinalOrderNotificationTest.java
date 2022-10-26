package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant2CanSwitchToSoleFinalOrderNotificationTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2CanSwitchToSoleFinalOrderNotification applicant2CanSwitchToSoleFinalOrderNotification;

    @Test
    public void shouldSendEmailToApplicant2SolicitorWhenJointApplication() {

        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(Solicitor
            .builder()
                .email(TEST_SOLICITOR_EMAIL)
            .build());

        Map<String, String> templateVars = solicitorTemplateVars(data, data.getApplicant2());
        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2())).thenReturn(templateVars);

        applicant2CanSwitchToSoleFinalOrderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH)
        );

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2());
    }

    @Test
    public void shouldNotSendEmailToApplicant2SolicitorWhenSoleApplication() {

        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YES);

        applicant2CanSwitchToSoleFinalOrderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
