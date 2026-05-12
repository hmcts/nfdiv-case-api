package uk.gov.hmcts.divorce.citizen.notification.interimapplications;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_SUBMITTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class GeneralApplicationD11SubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private GeneralApplicationD11SubmittedNotification notification;

    @Test
    void shouldSendNotificationForD11ApplicationSubmittedWithoutDocuments() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YesOrNo.YES)
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder().build())
            .build());
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationDocsUploadedPreSubmission(YesOrNo.NO)
            .generalApplicationFee(
                FeeDetails.builder()
                    .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF)
                    .build()
            )
            .build();

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant(data, TEST_CASE_ID, generalApplication);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendNotificationForD11ApplicationSubmittedWithAllDocuments() {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YesOrNo.YES)
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder().build())
            .build());
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationFee(
                FeeDetails.builder()
                    .dateOfPayment(LocalDate.of(2022, 1, 1))
                    .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD)
                    .build()
            )
            .build();

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant(data, TEST_CASE_ID, generalApplication);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_APPLICATION_D11_SUBMITTED,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }
}
