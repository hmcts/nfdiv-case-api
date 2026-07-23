package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SERVICE_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class SolicitorServiceApplicationSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SolicitorServiceApplicationSubmittedNotification notification;

    @ParameterizedTest
    @MethodSource("serviceType")
    void shouldSendServiceApplicationSubmittedNotification(AlternativeServiceType serviceType) {
        CaseData data = validCaseDataForIssueApplication();
        data.setAlternativeService(AlternativeService.builder()
                .serviceApplicationDocsUploadedPreSubmission(YesOrNo.NO)
                .alternativeServiceType(serviceType)
                .receivedServiceApplicationDate(LocalDate.of(2020, 1, 1))
            .build());

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.solicitorTemplateVarsPreIssue(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(TEST_SOLICITOR_EMAIL,
            SOLICITOR_SERVICE_APPLICATION_SUBMITTED,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    private static Stream<AlternativeServiceType> serviceType() {
        return Stream.of(DEEMED, BAILIFF, DISPENSED, ALTERNATIVE_SERVICE);
    }
}
