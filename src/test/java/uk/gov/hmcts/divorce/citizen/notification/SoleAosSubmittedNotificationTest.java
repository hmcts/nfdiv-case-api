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
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getCommonTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(SpringExtension.class)
public class SoleAosSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SoleAosSubmittedNotification notification;

    @Test
    void shouldSendAosNotDisputedEmailToSoleApplicantWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LocalDate.now().plusDays(141));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getCommonTemplateVars());

        notification.sendApplicationNotDisputedToApplicant(data, 1234567890123456L);

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
        final Map<String, String> templateVars = getCommonTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToApplicant(data, 1234567890123456L);

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
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getCommonTemplateVars());

        notification.sendApplicationNotDisputedToRespondent(data, 1234567890123456L);

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
        final Map<String, String> templateVars = getCommonTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToRespondent(data, 1234567890123456L);

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

}
