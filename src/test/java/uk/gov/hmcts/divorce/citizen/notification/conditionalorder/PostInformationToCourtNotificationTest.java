package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
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
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.SOLE_APPLICATION));

        postInformationToCourtNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO),
                hasEntry(WIFE_JOINT, CommonContent.NO),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToApplicant1WithDivorceContentIfLanguagePreferenceIsWelsh() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.SOLE_APPLICATION));

        postInformationToCourtNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO),
                hasEntry(WIFE_JOINT, CommonContent.NO),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotSendEmailToApplicant2WhenSoleApplication() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.JOINT_APPLICATION));

        postInformationToCourtNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentWhenJoint() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.setApplicationType(JOINT_APPLICATION);
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.JOINT_APPLICATION));

        postInformationToCourtNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES),
                hasEntry(WIFE_JOINT, CommonContent.YES),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendWelshEmailToApplicant2WithDivorceContentWhenJointAndLanguagePreferenceIsWelsh() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setApplicationType(JOINT_APPLICATION);
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(ApplicationType.JOINT_APPLICATION));

        postInformationToCourtNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(POST_INFORMATION_TO_COURT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES),
                hasEntry(WIFE_JOINT, CommonContent.YES),
                hasEntry(HUSBAND_JOINT, CommonContent.NO),
                hasEntry(CIVIL_PARTNER_JOINT, CommonContent.NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }
}
