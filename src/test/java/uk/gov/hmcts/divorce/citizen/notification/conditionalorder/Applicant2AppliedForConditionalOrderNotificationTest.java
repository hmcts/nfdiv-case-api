package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.APPLICANT1;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.APPLICANT2;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.CIVIL_PARTNER_APPLIED;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.CIVIL_PARTNER_DID_NOT_APPLY;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.HUSBAND_APPLIED;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.PARTNER_DID_NOT_APPLY;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.PARTNER_DID_NOT_APPLY_DUE_DATE;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.PLUS_14_DUE_DATE;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification.WIFE_DID_NOT_APPLY;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.CO_OR_FO;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.RESPONSE_DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_SUBMISSION_DATE_PLUS_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class Applicant2AppliedForConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant2AppliedForConditionalOrderNotification notification;

    @Test
    void shouldSendEmailToJointApplicant1WhoDidNotSubmitCo() {
        CaseData caseData = validApplicant1CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PLUS_14_DUE_DATE, getExpectedLocalDateTime().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WhoDidNotSubmitCoWithWelshContent() {
        CaseData caseData = validApplicant1CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        setMockClock(clock);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WhoDidNotSubmitCoButAlreadyApplied() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setSubmittedDate(caseData, List.of(APPLICANT2));
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, LocalDate.now().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendWelshEmailToJointApplicant1WhoDidNotSubmitCoButAlreadyApplied() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setSubmittedDate(caseData, List.of(APPLICANT2));
        setMockClock(clock);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToJointApplicant2WhoSubmittedCoWhenPartnerHasNotApplied() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(PARTNER_DID_NOT_APPLY, YES),
                hasEntry(WIFE_DID_NOT_APPLY, YES),
                hasEntry(CIVIL_PARTNER_DID_NOT_APPLY, NO),
                hasEntry(HUSBAND_APPLIED, NO),
                hasEntry(CIVIL_PARTNER_APPLIED, NO),
                hasEntry(PARTNER_DID_NOT_APPLY_DUE_DATE, getExpectedLocalDateTime().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendWelshEmailToJointApplicant2WhoSubmittedCoWhenPartnerHasNotApplied() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        setMockClock(clock);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLIED_FOR_CONDITIONAL_ORDER),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WhoSubmittedCoWhenPartnerApplied() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT1, APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PRONOUNCE_BY_DATE, LocalDate.now().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendWelshEmailToJointApplicant2WhoSubmittedCoWhenPartnerApplied() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT1, APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        setMockClock(clock);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2SolicitorWhoSubmittedCo() {
        CaseData data = validApplicant2CaseData();

        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx")
            .build());

        LocalDate issueDate = getExpectedLocalDate().minusDays(5);
        data.getApplication().setIssueDate(issueDate);
        setSubmittedDate(data, List.of(APPLICANT2));

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("app2sol@gm.com"),
            eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SOLICITOR_NAME, "app2sol"),
                hasEntry(SOLICITOR_REFERENCE, "refxxx"),
                hasEntry(RESPONSE_DUE_DATE, getExpectedLocalDateTime().plusDays(14).format(DATE_TIME_FORMATTER)),
                hasEntry(ISSUE_DATE, issueDate.format(DATE_TIME_FORMATTER)),
                hasEntry(CO_OR_FO, "conditional"),
                hasEntry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
                hasEntry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldNotSendEmailToApplicant2SolicitorWhenSoleApplication() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);

        notification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToJointApplicant1SolicitorToNotifyApplicant2HasSubmittedCO() {
        CaseData data = validApplicant2CaseData();

        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx")
            .build());

        LocalDate issueDate = getExpectedLocalDate().minusDays(5);
        data.getApplication().setIssueDate(issueDate);
        setSubmittedDate(data, List.of(APPLICANT2));

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("/signInUrl");

        setMockClock(clock);

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("app1sol@gm.com"),
            eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SOLICITOR_NAME, "app1sol"),
                hasEntry(SOLICITOR_REFERENCE, "refxxx"),
                hasEntry(ISSUE_DATE, issueDate.format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICANT_1_FULL_NAME, "test_first_name test_middle_name test_last_name"),
                hasEntry(APPLICANT_2_FULL_NAME, "test_first_name test_middle_name test_last_name"),
                hasEntry(SIGN_IN_URL,"/signInUrl")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    private void setSubmittedDate(CaseData caseData, List<String> applicants) {
        if (applicants.contains(APPLICANT1)) {
            caseData.getConditionalOrder()
                .setConditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(getExpectedLocalDateTime()).build());
        }
        if (applicants.contains(APPLICANT2)) {
            caseData.getConditionalOrder()
                .setConditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(getExpectedLocalDateTime()).build());
        }

    }
}
