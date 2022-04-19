package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(MockitoExtension.class)
class SoleApplicationSolicitorSubmitAosNotificationTest {

    private static final String ISSUE_DATE = "issueDate";
    private static final String IS_NOT_DISPUTED = "isNotDisputed";
    private static final String IS_DISPUTED = "isDisputed";
    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SoleApplicationSolicitorSubmitAosNotification soleApplicationSolicitorSubmitAosNotification;

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDivorceAndDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .howToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE).build());
        data.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.basicTemplateVars(data, 1234567890123456L))
            .thenReturn(getMainTemplateVars());

        soleApplicationSolicitorSubmitAosNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_NOT_DISPUTED, NO),
                hasEntry(IS_DISPUTED, YES),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, data.getApplication().getIssueDate().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, ""),
                hasEntry(ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
                )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDissolutionAndDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .howToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE).build());
        data.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.basicTemplateVars(data, 1234567890123456L))
            .thenReturn(getMainTemplateVars());

        soleApplicationSolicitorSubmitAosNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_NOT_DISPUTED, NO),
                hasEntry(IS_DISPUTED, YES),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, data.getApplication().getIssueDate().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, ""),
                hasEntry(ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDivorceAndNotDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .howToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE).build());
        data.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.basicTemplateVars(data, 1234567890123456L))
            .thenReturn(getMainTemplateVars());

        soleApplicationSolicitorSubmitAosNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_NOT_DISPUTED, YES),
                hasEntry(IS_DISPUTED, NO),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, ""),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER) + "."),
                hasEntry(ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendAosSubmittedEmailToRespondentSolicitorWithDissolutionAndNotDisputedContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .howToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE).build());
        data.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.basicTemplateVars(data, 1234567890123456L))
            .thenReturn(getMainTemplateVars());

        soleApplicationSolicitorSubmitAosNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_NOT_DISPUTED, YES),
                hasEntry(IS_DISPUTED, NO),
                hasEntry(ISSUE_DATE_PLUS_37_DAYS, ""),
                hasEntry(ISSUE_DATE_PLUS_141_DAYS, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER) + "."),
                hasEntry(ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }
}
