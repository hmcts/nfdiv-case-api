package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
class AwaitingConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private AwaitingConditionalOrderNotification conditionalOrderNotification;

    @Test
    void shouldSendEmailToApplicant1WithSubmissionResponseDate() {
        Map<String, Object> data = Map.of(
            APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME,
            APPLICANT_1_LAST_NAME, TEST_LAST_NAME,
            APPLICANT_2_FIRST_NAME, APPLICANT_2_FIRST_NAME,
            APPLICANT_2_LAST_NAME, APPLICANT_2_LAST_NAME,
            APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
            APPLICANT_1_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL,
            APPLICANT_1_LANGUAGE_PREFERENCE_WELSH, NO
        );

        conditionalOrderNotification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_AWAITING_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, "test_first_name test_last_name"),
                hasEntry(CCD_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(RESPONDENT_NAME, "applicant2FirstName applicant2LastName")
            )),
            eq(ENGLISH)
        );
    }
}
