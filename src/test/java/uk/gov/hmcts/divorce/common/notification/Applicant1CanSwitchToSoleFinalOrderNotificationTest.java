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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant1CanSwitchToSoleFinalOrderNotificationTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant1CanSwitchToSoleFinalOrderNotification applicant1CanSwitchToSoleFinalOrderNotification;

    @Test
    public void shouldSendEmailToApplicant1SolicitorWhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(Solicitor
            .builder()
                .email(TEST_SOLICITOR_EMAIL)
            .build());

        Map<String, String> templateVars = solicitorTemplateVars(data, data.getApplicant1());
        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1())).thenReturn(templateVars);

        applicant1CanSwitchToSoleFinalOrderNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH)
        );

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1());
    }

    @Test
    public void shouldNotSendEmailToApplicant1SolicitorWhenSoleApplication() {

        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YES);

        applicant1CanSwitchToSoleFinalOrderNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
