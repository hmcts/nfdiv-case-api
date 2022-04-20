package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_UNDISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(SpringExtension.class)
public class SoleApplicationNotDisputedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    @Test
    void shouldSendAosNotDisputedEmailToSoleApplicantWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LocalDate.now().plusDays(141));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        soleApplicationNotDisputedNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry("apply for CO date", data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleApplicantWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        soleApplicationNotDisputedNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry("apply for CO date", data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        soleApplicationNotDisputedNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry("apply for CO date", data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        soleApplicationNotDisputedNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry("apply for CO date", data.getDueDate().format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosDisputedEmailToApplicant1SolicitorWithCorrectContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICANT_NAME,
            join(" ", data.getApplicant1().getFirstName(), data.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", data.getApplicant2().getFirstName(), data.getApplicant2().getLastName()));
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(templateVars);
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        soleApplicationNotDisputedNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

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
                hasEntry(IS_UNDISPUTED, YES),
                hasEntry(IS_DISPUTED, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, ""),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).basicTemplateVars(data, TEST_CASE_ID);
    }
}
