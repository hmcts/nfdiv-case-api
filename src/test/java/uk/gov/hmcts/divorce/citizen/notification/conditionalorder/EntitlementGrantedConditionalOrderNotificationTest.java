package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING_MINUS_SEVEN_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseWithCourtHearing;

@ExtendWith(SpringExtension.class)
class EntitlementGrantedConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private EntitlementGrantedConditionalOrderNotification entitlementGrantedConditionalOrderNotification;

    @Test
    void shouldSendEmailToApplicant1WithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithCourtHearingContent() {
        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        entitlementGrantedConditionalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED),
            argThat(allOf(
                hasEntry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
                hasEntry(DATE_OF_HEARING, "8 November 2021"),
                hasEntry(TIME_OF_HEARING, "14:56 pm"),
                hasEntry(DATE_OF_HEARING_MINUS_SEVEN_DAYS, "1 November 2021")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }
}
