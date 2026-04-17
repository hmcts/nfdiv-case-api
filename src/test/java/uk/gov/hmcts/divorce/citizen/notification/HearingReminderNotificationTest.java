package uk.gov.hmcts.divorce.citizen.notification;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.notification.HearingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.HearingAttendance;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.notification.HearingReminderNotification.HEARING_ATTENDANCE_MODE;
import static uk.gov.hmcts.divorce.common.notification.HearingReminderNotification.HEARING_DATE;
import static uk.gov.hmcts.divorce.common.notification.HearingReminderNotification.HEARING_REMINDER_LETTER_TYPE;
import static uk.gov.hmcts.divorce.common.notification.HearingReminderNotification.HEARING_TIME;
import static uk.gov.hmcts.divorce.common.notification.HearingReminderNotification.HEARING_VENUE;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.HEARING_REMINDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class HearingReminderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private HearingReminderNotification hearingReminderNotification;

    @Test
    void shouldSendReminderEmailToApplicant1() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.setHearing(buildHearingData());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(new HashMap<>());

        hearingReminderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(HEARING_REMINDER_CITIZEN),
            argThat(allOf(
                hasEntry(HEARING_DATE, "1 January 2022"),
                hasEntry(HEARING_TIME, "12:00 pm"),
                hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant2() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_EMAIL);
        data.setHearing(buildHearingData());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(new HashMap<>());

        hearingReminderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_EMAIL),
            eq(HEARING_REMINDER_CITIZEN),
            argThat(allOf(
                hasEntry(HEARING_DATE, "1 January 2022"),
                hasEntry(HEARING_TIME, "12:00 pm"),
                hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendReminderEmailToApplicant1Solicitor() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().getSolicitor().setEmail(TEST_USER_EMAIL);
        data.setHearing(buildHearingData());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(new HashMap<>());

        hearingReminderNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(HEARING_REMINDER_SOLICITOR),
            argThat(allOf(
                hasEntry(HEARING_DATE, "1 January 2022"),
                hasEntry(HEARING_TIME, "12:00 pm"),
                hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1());
    }

    @Test
    void shouldSendReminderEmailToApplicant2Solicitor() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_USER_EMAIL)
                .build()
        );
        data.setHearing(buildHearingData());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(new HashMap<>());

        hearingReminderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(HEARING_REMINDER_SOLICITOR),
            argThat(allOf(
                hasEntry(HEARING_DATE, "1 January 2022"),
                hasEntry(HEARING_TIME, "12:00 pm"),
                hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2());
    }

    @Test
    void shouldSendReminderLetterToApplicant1() {
        CaseData data = validCaseDataForIssueApplication();
        Applicant applicant = data.getApplicant1();
        data.setHearing(buildHearingData());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference()))
            .thenReturn(new HashMap<>());

        Document document = Document.builder().build();

        when(caseDataDocumentService.renderDocument(
            anyMap(),
            eq(TEST_CASE_ID),
            eq(HEARING_REMINDER),
            eq(applicant.getLanguagePreference()),
            eq(HEARING_REMINDER_LETTER_TYPE)
        )).thenReturn(document);

        UUID expectedLetterId = UUID.randomUUID();
        when(bulkPrintService.print(any(Print.class))).thenReturn(expectedLetterId);

        hearingReminderNotification.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(caseDataDocumentService).renderDocument(
            argThat(allOf(
                Matchers.<String, Object>hasEntry(NAME, applicant.getFullName()),
                Matchers.<String, Object>hasEntry(ADDRESS, AddressUtil.getPostalAddress(applicant.getAddress())),
                Matchers.<String, Object>hasEntry(IS_DIVORCE, Boolean.valueOf(data.isDivorce())),
                Matchers.<String, Object>hasEntry(CASE_REFERENCE, formatId(TEST_CASE_ID)),
                Matchers.<String, Object>hasEntry(HEARING_DATE, "1 January 2022"),
                Matchers.<String, Object>hasEntry(HEARING_TIME, "12:00 pm"),
                Matchers.<String, Object>hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                Matchers.<String, Object>hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(TEST_CASE_ID),
            eq(HEARING_REMINDER),
            eq(applicant.getLanguagePreference()),
            eq(HEARING_REMINDER_LETTER_TYPE)
        );
    }

    @Test
    void shouldSendReminderLetterToApplicant2() {
        CaseData data = validCaseDataForIssueApplication();
        Applicant applicant = data.getApplicant2();
        data.setHearing(buildHearingData());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference()))
            .thenReturn(new HashMap<>());

        Document document = Document.builder().build();

        when(caseDataDocumentService.renderDocument(
            anyMap(),
            eq(TEST_CASE_ID),
            eq(HEARING_REMINDER),
            eq(applicant.getLanguagePreference()),
            eq(HEARING_REMINDER_LETTER_TYPE)
        )).thenReturn(document);

        UUID expectedLetterId = UUID.randomUUID();
        when(bulkPrintService.print(any(Print.class))).thenReturn(expectedLetterId);

        hearingReminderNotification.sendToApplicant2Offline(data, TEST_CASE_ID);

        verify(caseDataDocumentService).renderDocument(
            argThat(allOf(
                Matchers.<String, Object>hasEntry(NAME, applicant.getFullName()),
                Matchers.<String, Object>hasEntry(ADDRESS, AddressUtil.getPostalAddress(applicant.getAddress())),
                Matchers.<String, Object>hasEntry(IS_DIVORCE, Boolean.valueOf(data.isDivorce())),
                Matchers.<String, Object>hasEntry(CASE_REFERENCE, formatId(TEST_CASE_ID)),
                Matchers.<String, Object>hasEntry(HEARING_DATE, "1 January 2022"),
                Matchers.<String, Object>hasEntry(HEARING_TIME, "12:00 pm"),
                Matchers.<String, Object>hasEntry(HEARING_VENUE, BIRMINGHAM.getLabel()),
                Matchers.<String, Object>hasEntry(HEARING_ATTENDANCE_MODE, HearingAttendance.IN_PERSON.getLabel())
            )),
            eq(TEST_CASE_ID),
            eq(HEARING_REMINDER),
            eq(applicant.getLanguagePreference()),
            eq(HEARING_REMINDER_LETTER_TYPE)
        );
    }

    public Hearing buildHearingData() {
        return Hearing.builder()
            .dateOfHearing(LocalDateTime.of(2022, 1, 1, 12, 0, 0))
            .venueOfHearing(BIRMINGHAM.getLabel())
            .hearingAttendance(Set.of(HearingAttendance.IN_PERSON))
            .build();
    }
}
