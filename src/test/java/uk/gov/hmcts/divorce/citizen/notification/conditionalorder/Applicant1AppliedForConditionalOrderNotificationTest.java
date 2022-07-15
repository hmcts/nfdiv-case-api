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
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
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
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.PLUS_21_DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT1_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class Applicant1AppliedForConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant1AppliedForConditionalOrderNotification notification;

    private static final String CO_REVIEWED_BY_DATE = "date email received plus 21 days";

    @Test
    void shouldSendEmailToSoleApplicant1WhoSubmittedCoWithDivorceContent() {
        CaseData data = caseData(DIVORCE, ApplicationType.SOLE_APPLICATION);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(CO_REVIEWED_BY_DATE, getExpectedLocalDateTime().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleApplicant1WhoSubmittedCoWithDissolutionContent() {
        CaseData data = caseData(DISSOLUTION, ApplicationType.SOLE_APPLICATION);
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        setMockClock(clock);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(CO_REVIEWED_BY_DATE, getExpectedLocalDateTime().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToSoleApplicant1WhoSubmittedCoWithDivorceContentIfLanguagePreferenceIsWelsh() {
        CaseData data = caseData(DIVORCE, ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(CO_REVIEWED_BY_DATE, getExpectedLocalDateTime().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(WELSH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToSoleApplicant1WhoSubmittedCoWithDissolutionContentIfLanguagePreferenceIsWelsh() {
        CaseData data = caseData(DISSOLUTION, ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        setMockClock(clock);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(CO_REVIEWED_BY_DATE, getExpectedLocalDateTime().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(WELSH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WhoSubmittedCoWhenPartnerHasNotApplied() {
        CaseData data = caseData(DISSOLUTION, ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(PARTNER_DID_NOT_APPLY, YES),
                hasEntry(WIFE_DID_NOT_APPLY, NO),
                hasEntry(CIVIL_PARTNER_DID_NOT_APPLY, YES),
                hasEntry(HUSBAND_APPLIED, NO),
                hasEntry(CIVIL_PARTNER_APPLIED, NO),
                hasEntry(PARTNER_DID_NOT_APPLY_DUE_DATE, getExpectedLocalDateTime().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WhoSubmittedCoWhenPartnerApplied() {
        CaseData caseData = caseData(DIVORCE, ApplicationType.JOINT_APPLICATION);
        setSubmittedDate(caseData, List.of(APPLICANT2));
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant1(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PLUS_21_DUE_DATE, LocalDate.now().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleApplicant1SolicitorWhoSubmittedCo() {
        final LocalDate date = LocalDate.of(2021, 6, 18);
        final CaseData data = caseData(DIVORCE, ApplicationType.SOLE_APPLICATION);
        data.getApplication().setIssueDate(date);
        data.setDueDate(date);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name(TEST_SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .reference("REF01")
                .build()
        );
        final Map<String, String> templateVars = getBasicTemplateVars();

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(templateVars);
        setMockClock(clock);

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT1_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(ISSUE_DATE, date.format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(SOLICITOR_REFERENCE, "REF01")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).basicTemplateVars(data, 1234567890123456L);
    }

    @Test
    void shouldNotSendEmailToSoleApplicant1SolicitorIfJointApplication() {
        CaseData data = caseData(DIVORCE, ApplicationType.JOINT_APPLICATION);

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToJointApplicant2WhoDidNotSubmitCo() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT1));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());
        setMockClock(clock);

        notification.sendToApplicant2(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PLUS_14_DUE_DATE, getExpectedLocalDateTime().plusDays(14).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WhoDidNotSubmitCoWithWelshContent() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT1));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateVars);
        setMockClock(clock);

        notification.sendToApplicant2(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH)
        );
        verify(commonContent).mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WhoDidNotSubmitCoButAlreadyApplied() {
        CaseData caseData = validApplicant2CaseData();
        setSubmittedDate(caseData, List.of(APPLICANT1, APPLICANT2));
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant2(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(PLUS_21_DUE_DATE, LocalDate.now().plusDays(21).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldNotSendEmailToSoleApplicant2WhoDidNotSubmitCo() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        setMockClock(clock);

        notification.sendToApplicant2(caseData, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    private CaseData caseData(DivorceOrDissolution divorceOrDissolution, ApplicationType applicationType) {
        CaseData data = validApplicant1CaseData();
        data.setDivorceOrDissolution(divorceOrDissolution);
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        data.setApplicationType(applicationType);
        return data;
    }

    private void setSubmittedDate(CaseData caseData, List<String> applicants) {
        if (applicants.contains(APPLICANT1)) {
            caseData.getConditionalOrder()
                .setConditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build());
        }
        if (applicants.contains(APPLICANT2)) {
            caseData.getConditionalOrder()
                .setConditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build());
        }

    }
}
