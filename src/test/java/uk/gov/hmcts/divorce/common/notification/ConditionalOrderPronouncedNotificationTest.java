package uk.gov.hmcts.divorce.common.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT1_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT2_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPronouncedNotificationTest {

    private static final int FINAL_ORDER_OFFSET_DAYS = 43;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    private static final DocumentPackInfo APPLICANT_1_TEST_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_TEST_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );

    static final String LETTER_ID = "letterId";

    @Mock
    private LetterPrinter letterPrinter;

    @Mock
    private ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocumentPack;

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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentForSoleApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentForSoleApplication() {
        ReflectionTestUtils.setField(notification, "finalOrderOffsetDays", FINAL_ORDER_OFFSET_DAYS);

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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
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

        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
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
        when(commonContent.coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            any(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).coPronouncedTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
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

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant"),
                hasEntry(APPLICANT2_LABEL, "Respondent"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
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

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));
        when(commonContent.getUnionType(data)).thenReturn("dissolution");

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant 1"),
                hasEntry(APPLICANT2_LABEL, "Applicant 2"),
                hasEntry(UNION_TYPE, "dissolution")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
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

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant 1"),
                hasEntry(APPLICANT2_LABEL, "Applicant 2"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldPrintLetterToApplicant1IfOffline() {

        CaseData data = caseData();
        when(conditionalOrderPronouncedDocumentPack.getDocumentPack(data, data.getApplicant1()))
            .thenReturn(APPLICANT_1_TEST_PACK_INFO);
        when(conditionalOrderPronouncedDocumentPack.getLetterId()).thenReturn(LETTER_ID);

        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        notification.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture()
        );

        assertEquals(data, captorData.getValue());
        assertEquals(TEST_CASE_ID, captorCaseId.getValue());
        assertEquals(data.getApplicant1(), captorApplicant.getValue());
        assertEquals(APPLICANT_1_TEST_PACK_INFO, captorDocumentPackInfo.getValue());
        assertEquals(LETTER_ID, captorLetterId.getValue());
    }

    @Test
    void shouldSendLetterToApplicant2IfOffline() {

        CaseData data = validApplicant2CaseData();
        when(conditionalOrderPronouncedDocumentPack.getDocumentPack(data, data.getApplicant2()))
            .thenReturn(APPLICANT_2_TEST_PACK_INFO);
        when(conditionalOrderPronouncedDocumentPack.getLetterId()).thenReturn(LETTER_ID);

        ArgumentCaptor<CaseData> captorData = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Long> captorCaseId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Applicant> captorApplicant = ArgumentCaptor.forClass(Applicant.class);
        ArgumentCaptor<DocumentPackInfo> captorDocumentPackInfo = ArgumentCaptor.forClass(DocumentPackInfo.class);
        ArgumentCaptor<String> captorLetterId = ArgumentCaptor.forClass(String.class);

        notification.sendToApplicant2Offline(data, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            captorData.capture(),
            captorCaseId.capture(),
            captorApplicant.capture(),
            captorDocumentPackInfo.capture(),
            captorLetterId.capture()
        );

        assertEquals(data, captorData.getValue());
        assertEquals(TEST_CASE_ID, captorCaseId.getValue());
        assertEquals(data.getApplicant2(), captorApplicant.getValue());
        assertEquals(APPLICANT_2_TEST_PACK_INFO, captorDocumentPackInfo.getValue());
        assertEquals(LETTER_ID, captorLetterId.getValue());
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

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));
        when(commonContent.getUnionType(data)).thenReturn("divorce");

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICANT1_LABEL, "Applicant"),
                hasEntry(APPLICANT2_LABEL, "Respondent"),
                hasEntry(UNION_TYPE, "divorce")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
