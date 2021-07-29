package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR_IT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2ApprovedCaseDataMap;

@ExtendWith(SpringExtension.class)
public class Applicant2ApprovedApplicant1NotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2ApprovedApplicant1Notification notification;

    @Test
    void shouldSendEmailWithDivorceContent() {
        CaseData data = validApplicant2ApprovedCaseDataMap();
        data.getApplication().getHelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setApplicant2ApprovedDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant2())).thenReturn("husband");

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required: you"),
                hasEntry(PARTNER, "husband"),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, " and paid")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1());
        verify(commonContent).getTheirPartner(data, data.getApplicant2());
    }

    @Test
    void shouldSendEmailWithDissolutionContent() {
        CaseData data = validApplicant2ApprovedCaseDataMap();
        data.getApplication().getHelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setApplicant2ApprovedDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant2())).thenReturn("civil partner");

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required: you"),
                hasEntry(PARTNER, "civil partner"),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, " and paid")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1());
        verify(commonContent).getTheirPartner(data, data.getApplicant2());
    }
}

