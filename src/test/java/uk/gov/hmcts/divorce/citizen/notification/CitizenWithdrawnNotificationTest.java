package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_WITHDRAWN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class CitizenWithdrawnNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private CitizenWithdrawnNotification notification;

    @Test
    void shouldSendApplicant1CitizenWithdrawnNotification() {
        CaseData data = validApplicant1CaseData();
        final Map<String, String> templateVars = Map.of(
            FIRST_NAME, data.getApplicant1().getFirstName(),
            LAST_NAME, data.getApplicant1().getLastName()
        );
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_WITHDRAWN),
            argThat(allOf(
                hasEntry(FIRST_NAME, data.getApplicant1().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant1().getLastName())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2CitizenWithdrawnNotificationIfApplicationTypeIsJoint() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);

        final Map<String, String> templateVars = Map.of(
            FIRST_NAME, data.getApplicant2().getFirstName(),
            LAST_NAME, data.getApplicant2().getLastName()
        );
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);
        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_WITHDRAWN),
            argThat(allOf(
                hasEntry(FIRST_NAME, data.getApplicant2().getFirstName()),
                hasEntry(LAST_NAME, data.getApplicant2().getLastName())
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant2CitizenWithdrawnNotificationIfApplicationTypeIsSole() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }
}
