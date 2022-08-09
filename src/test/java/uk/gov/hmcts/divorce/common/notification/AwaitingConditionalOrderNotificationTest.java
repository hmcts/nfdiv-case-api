package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.print.ApplyForConditionalOrderPrinter;

import java.time.LocalDate;
import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class AwaitingConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private ApplyForConditionalOrderPrinter applyForConditionalOrderPrinter;

    @InjectMocks
    private AwaitingConditionalOrderNotification notification;

    @Test
    void shouldSendEmailToApplicant1() {
        final var data = validApplicant1CaseData();
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(new HashMap<>());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorSoleNotification() {
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .applicant1(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(1234567890123456L)).thenReturn(SIGN_IN_PROFESSIONAL_USERS_URL);
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "not provided"),
                hasEntry(SIGN_IN_URL, SIGN_IN_PROFESSIONAL_USERS_URL)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorJointNotification() {
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .applicant1(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "not provided"),
                hasEntry(IS_CONDITIONAL_ORDER, YES),
                hasEntry(IS_FINAL_ORDER, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorJointNotification() {
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .applicant2(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        notification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "not provided"),
                hasEntry(IS_CONDITIONAL_ORDER, YES),
                hasEntry(IS_FINAL_ORDER, NO)
                )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithReference() {
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .name(TEST_SOLICITOR_NAME)
                .reference("ref")
                .build()
        );
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .applicant1(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_REFERENCE, "ref")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldCanApplyForConditionalOrderLetterToApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant2(FEMALE));

        notification.sendToApplicant1Offline(caseData, 1234567890123456L);

        verify(applyForConditionalOrderPrinter).sendLetters(
            caseData,
            1234567890123456L,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );
    }

    @Test
    void shouldSendEmailToApplicant2IfJointApplication() {
        final var data = validApplicant2CaseData();
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(new HashMap<>());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendEmailToApplicant2IfSoleApplication() {
        final var data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2(data, 1234567890123456L);

        verifyNoInteractions(notificationService, commonContent);
    }

    @Test
    void shouldNotSendEmailToApplicant2IfEmailNotSet() {
        final var data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, 1234567890123456L);

        verifyNoInteractions(notificationService, commonContent);
    }

    @Test
    void shouldNotSendLetterToApplicant2IfSoleCase() {
        final var data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2Offline(data, 1234567890123456L);

        verifyNoInteractions(applyForConditionalOrderPrinter);
    }

    @Test
    void shouldSendCanApplyForConditionalOrderLetterToApplicant2IfJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant2(FEMALE));

        notification.sendToApplicant2Offline(caseData, 1234567890123456L);

        verify(applyForConditionalOrderPrinter).sendLetters(
            caseData,
            1234567890123456L,
            caseData.getApplicant2(),
            caseData.getApplicant1()
        );
    }
}
