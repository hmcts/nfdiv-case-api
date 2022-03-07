package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;

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
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(MockitoExtension.class)
class SoleApplicationDisputedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Test
    void shouldSendAosDisputedEmailToSoleApplicantWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.now());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getAcknowledgementOfService().getDateAosSubmitted().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleApplicantWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.now());
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        soleApplicationDisputedNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getAcknowledgementOfService().getDateAosSubmitted().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.now());
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        soleApplicationDisputedNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getAcknowledgementOfService().getDateAosSubmitted().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosDisputedEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.now());
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        soleApplicationDisputedNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE,
                    data.getAcknowledgementOfService().getDateAosSubmitted().plusDays(37).format(DATE_TIME_FORMATTER)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }
}
