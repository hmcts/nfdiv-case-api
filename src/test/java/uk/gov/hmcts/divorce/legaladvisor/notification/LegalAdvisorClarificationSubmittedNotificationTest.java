package uk.gov.hmcts.divorce.legaladvisor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
public class LegalAdvisorClarificationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private LegalAdvisorClarificationSubmittedNotification notification;

    @Test
    void shouldSendClarificationSubmittedEmailToSolicitor() {

        var data = CaseData
            .builder()
            .applicant1(
                Applicant
                    .builder()
                    .firstName(TEST_FIRST_NAME)
                    .lastName(TEST_LAST_NAME)
                    .languagePreferenceWelsh(NO)
                    .solicitor(
                        Solicitor
                            .builder()
                            .email(TEST_SOLICITOR_EMAIL)
                            .name(TEST_SOLICITOR_NAME)
                            .build()
                    )
                    .build()
            )
            .applicant2(
                Applicant
                    .builder()
                    .firstName(APPLICANT_2_FIRST_NAME)
                    .lastName(APPLICANT_2_LAST_NAME)
                    .build()
            )
            .build();

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICANT_NAME,
            join(" ", data.getApplicant1().getFirstName(), data.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", data.getApplicant2().getFirstName(), data.getApplicant2().getLastName()));
        templateVars.put(APPLICATION_REFERENCE, formatId(1234567890123456L));

        when(commonContent.basicTemplateVars(eq(data), eq(1234567890123456L)))
            .thenReturn(templateVars);

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_CLARIFICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, "test_first_name test_last_name"),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(RESPONDENT_NAME, "applicant2FirstName applicant2LastName")
            )),
            eq(ENGLISH)
        );
    }
}
