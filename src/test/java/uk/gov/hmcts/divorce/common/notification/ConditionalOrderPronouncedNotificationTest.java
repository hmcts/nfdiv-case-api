package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT1_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT2_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.WELSH_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPronouncedNotificationTest {

    private static final int FINAL_ORDER_OFFSET_DAYS = 43;
    private static final int FINAL_ORDER_RESPONDENT_OFFSET_MONTHS = 3;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private ConditionalOrderPronouncedPrinter printer;

    @InjectMocks
    private ConditionalOrderPronouncedNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, data.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, data.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentForSoleApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);
        ReflectionTestUtils.setField(notification, "finalOrderRespondentOffsetMonth", FINAL_ORDER_RESPONDENT_OFFSET_MONTHS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, now.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentForSoleApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);
        ReflectionTestUtils.setField(notification, "finalOrderRespondentOffsetMonth", FINAL_ORDER_RESPONDENT_OFFSET_MONTHS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, now.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentForJointApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, now.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentForJointApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, now.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldThrowNotificationTemplateExceptionIfCourtIsNotSet() {

        final LocalDateTime now = LocalDateTime.now();
        final CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        assertThatExceptionOfType(NotificationTemplateException.class)
            .isThrownBy(() -> notification.sendToApplicant1(data, 1234567890123456L))
            .withMessage("Notification failed with missing field 'coCourt' for Case Id: 1234567890123456");
    }

    @Test
    void shouldThrowNotificationTemplateExceptionIfDateAndTimeOfHearingIsNotSet() {

        final LocalDateTime now = LocalDateTime.now();
        final CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .grantedDate(now.toLocalDate())
            .build()
        );

        assertThatExceptionOfType(NotificationTemplateException.class)
            .isThrownBy(() -> notification.sendToApplicant1(data, 1234567890123456L))
            .withMessage("Notification failed with missing field 'coDateAndTimeOfHearing' for Case Id: 1234567890123456");
    }

    @Test
    void shouldThrowNotificationTemplateExceptionIfGrantedDateIsNotSet() {

        final LocalDateTime now = LocalDateTime.now();
        final CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .build()
        );

        assertThatExceptionOfType(NotificationTemplateException.class)
            .isThrownBy(() -> notification.sendToApplicant1(data, 1234567890123456L))
            .withMessage("Notification failed with missing field 'coGrantedDate' for Case Id: 1234567890123456");
    }

    @Test
    void shouldSendWelshEmailToApplicant1WithDivorceContent() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, data.getConditionalOrder().getDateAndTimeOfHearing().format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH)
        );

        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToApplicant2WithDissolutionContentForJointApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        LocalDateTime now = LocalDateTime.now();

        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(now)
            .grantedDate(now.toLocalDate())
            .build()
        );

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMINGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, now.format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWhenSoleApplicationAndApplicant1IsRepresented() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicant1(applicantRepresentedBySolicitor());
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(now.minusDays(10).toLocalDate());
        data.setConditionalOrder(ConditionalOrder.builder()
            .grantedDate(now.toLocalDate())
            .build()
        );

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant"),
                hasEntry(APPLICANT2_LABEL, "Respondent"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWhenJointApplicationAndApplicant1IsRepresented() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicant1(applicantRepresentedBySolicitor());
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(now.minusDays(10).toLocalDate());
        data.setConditionalOrder(ConditionalOrder.builder()
            .grantedDate(now.toLocalDate())
            .build()
        );

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));
        when(commonContent.getUnionType(data)).thenReturn("dissolution");

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant 1"),
                hasEntry(APPLICANT2_LABEL, "Applicant 2"),
                hasEntry(UNION_TYPE, "dissolution")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWhenJointApplicationAndApplicant2IsRepresented() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(now.minusDays(10).toLocalDate());
        data.setConditionalOrder(ConditionalOrder.builder()
            .grantedDate(now.toLocalDate())
            .build()
        );

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant 1"),
                hasEntry(APPLICANT2_LABEL, "Applicant 2"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendLetterToApplicant1IfOffline() {
        CaseData data = CaseData.builder().build();

        notification.sendToApplicant1Offline(data, 1234567890123456L);

        verify(printer).sendLetter(
            eq(data),
            eq(1234567890123456L),
            eq(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1),
            eq(data.getApplicant1())
        );
    }

    @Test
    void shouldSendLetterToApplicant2IfOffline() {
        CaseData data = CaseData.builder()
            .build();

        notification.sendToApplicant2Offline(data, 1234567890123456L);

        verify(printer).sendLetter(
            eq(data),
            eq(1234567890123456L),
            eq(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2),
            eq(data.getApplicant2())
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWhenSoleApplicationAndApplicant2IsRepresented() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

        LocalDateTime now = LocalDateTime.now();
        CaseData data = caseData();
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(now.minusDays(10).toLocalDate());
        data.setConditionalOrder(ConditionalOrder.builder()
            .grantedDate(now.toLocalDate())
            .build()
        );

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant"),
                hasEntry(APPLICANT2_LABEL, "Respondent"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH)
        );
    }
}
