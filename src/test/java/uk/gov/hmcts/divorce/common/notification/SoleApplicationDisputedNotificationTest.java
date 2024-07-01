package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.notification.SoleApplicationDisputedNotification.DISPUTED_AOS_FEE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_UNDISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.WELSH_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(MockitoExtension.class)
class SoleApplicationDisputedNotificationTest {

    private static final int DISPUTE_DUE_DATE_OFFSET_DAYS = 37;
    private static final String DISPUTE_FEE = "270";
    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Test
    void shouldSendAosDisputedEmailToSoleApplicantWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LocalDate.now());
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getApplication().getIssueDate()
                        .plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleApplicantWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setIssueDate(LocalDate.now());
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        soleApplicationDisputedNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getApplication().getIssueDate()
                        .plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LocalDate.now());
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getApplication().getIssueDate()
                        .plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleRespondentWithDivorceContentWhenLangPrefIsWelsh() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getApplication().setIssueDate(LocalDate.now());
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
        data.getApplicant2().setEmail(null);

        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        soleApplicationDisputedNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getApplication().getIssueDate()
                        .plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(WELSH_DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setIssueDate(LocalDate.now());
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputedAOSFee", DISPUTE_FEE);
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        soleApplicationDisputedNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getApplication().getIssueDate()
                        .plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(DISPUTED_AOS_FEE,DISPUTE_FEE)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosDisputedEmailToApplicant1SolicitorWithCorrectContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplicant1().getSolicitor().setName(TEST_SOLICITOR_NAME);
        data.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getMainTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        soleApplicationDisputedNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(SOLICITOR_NAME, data.getApplicant1().getSolicitor().getName()),
                hasEntry(SOLICITOR_REFERENCE, data.getApplicant1().getSolicitor().getReference()),
                hasEntry(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL),
                hasEntry(IS_UNDISPUTED, NO),
                hasEntry(IS_DISPUTED, YES),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS,
                    LocalDate.of(2021, 6, 18).plusDays(DISPUTE_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDivorceAndDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName()),
                hasEntry(SOLICITOR_REFERENCE, NOT_PROVIDED),
                hasEntry(IS_UNDISPUTED, NO),
                hasEntry(IS_DISPUTED, YES),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, data.getApplication().getIssueDate().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, ""),
                hasEntry(DATE_OF_ISSUE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDissolutionAndDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        ReflectionTestUtils.setField(soleApplicationDisputedNotification, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(SOLICITOR_NAME, data.getApplicant2().getSolicitor().getName()),
                hasEntry(SOLICITOR_REFERENCE, NOT_PROVIDED),
                hasEntry(IS_UNDISPUTED, NO),
                hasEntry(IS_DISPUTED, YES),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, data.getApplication().getIssueDate().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, ""),
                hasEntry(DATE_OF_ISSUE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
