package uk.gov.hmcts.divorce.citizen.notification.interimapplications;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED_HWF;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class SearchGovRecordsApplicationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SearchGovRecordsApplicationSubmittedNotification notification;

    @Test
    void shouldSendNotificationForSearchGovRecordsApplicationSubmitted() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
                .interimAppsUseHelpWithFees(YesOrNo.NO)
                .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
                .searchGovRecordsJourneyOptions(SearchGovRecordsJourneyOptions.builder().applicationSubmittedDate(LocalDate.now()).build())
                .build());

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendNotificationForSearchGovRecordsApplicationSubmittedWithHelpWithFees() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YesOrNo.YES)
            .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
            .searchGovRecordsJourneyOptions(SearchGovRecordsJourneyOptions.builder().build())
            .build());

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED_HWF,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }
}
