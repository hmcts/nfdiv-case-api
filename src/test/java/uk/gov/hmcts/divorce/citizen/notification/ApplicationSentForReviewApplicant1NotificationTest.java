package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;


@ExtendWith(MockitoExtension.class)
public class ApplicationSentForReviewApplicant1NotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationSentForReviewApplicant1Notification notification;

    @Test
    void shouldSendEmailToApplicant1WhileInAwaitingApplicant2ResponseState() {
        CaseData data = caseData();
        data.setDueDate(LOCAL_DATE);
        data.setApplicant2(getApplicant2(Gender.MALE));
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant2())).thenReturn("husband");

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(PARTNER, "husband")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsFor(data);
        verify(commonContent).getTheirPartner(data, data.getApplicant2());
    }
}
