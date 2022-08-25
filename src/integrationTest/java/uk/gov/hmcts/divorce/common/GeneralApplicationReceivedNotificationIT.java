package uk.gov.hmcts.divorce.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DISPENSE_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_RECEIVED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GeneralApplicationReceivedNotificationIT {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private GeneralApplicationReceivedNotification generalApplicationReceivedNotification;

    @Test
    void shouldSendEmailToApplicant1WithEnglishContentIfApplicant1LanguagePreferenceIsNotWelsh() {

        final CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        data.getAlternativeService().setAlternativeServiceType(DEEMED);

        generalApplicationReceivedNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_APPLICATION_RECEIVED),
            argThat(allOf(
                hasEntry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net"),
                hasEntry(IS_BAILIFF_SERVICE, NO),
                hasEntry(COURT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(PARTNER, "husband"),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DEEMED_SERVICE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISPENSE_SERVICE, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithWelshContentIfApplicant1LanguagePreferenceWelsh() {

        final CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(DEEMED);

        generalApplicationReceivedNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_APPLICATION_RECEIVED),
            argThat(allOf(
                hasEntry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net"),
                hasEntry(IS_BAILIFF_SERVICE, NO),
                hasEntry(COURT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(PARTNER, "gŵr"),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DEEMED_SERVICE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISPENSE_SERVICE, NO)
            )),
            eq(WELSH)
        );
    }
}
