package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.POST_INFORMATION_TO_COURT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class PostInformationToCourtNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private PostInformationToCourtNotification postInformationToCourtNotification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant1CaseData();
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.SOLE_APPLICATION));

        postInformationToCourtNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO),
                hasEntry(WIFE_JOINT, CommonContent.NO),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotSendEmailToApplicant2WhenSoleApplication() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.JOINT_APPLICATION));

        postInformationToCourtNotification.sendToApplicant2(data, 1234567890123456L);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentWhenJoint() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.setApplicationType(JOINT_APPLICATION);
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.JOINT_APPLICATION));

        postInformationToCourtNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES),
                hasEntry(WIFE_JOINT, CommonContent.YES),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }
}
