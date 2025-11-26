package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED_BY_CASEWORKER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class ServiceApplicationRejectedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ServiceApplicationRejectedNotification notification;

    @Test
    void shouldSendNotificationForApplicant1() {
        CaseData data = validCaseDataForIssueApplication();
        data.getAlternativeService().setAlternativeServiceType(AlternativeServiceType.DEEMED);

        Map<String, String> templateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            SERVICE_APPLICATION_REJECTED_BY_CASEWORKER,
            templateVars,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    static Stream<Arguments> provideTestArguments() {
        return Stream.of(
            Arguments.of(AlternativeServiceType.DEEMED, false, "deemed service"),
            Arguments.of(AlternativeServiceType.DEEMED, true, "cyflwyno tybiedig"),
            Arguments.of(AlternativeServiceType.DISPENSED, false, "dispensed with service"),
            Arguments.of(AlternativeServiceType.DISPENSED, true, "hepgor cyflwyno"),
            Arguments.of(AlternativeServiceType.BAILIFF, false, "bailiff service"),
            Arguments.of(AlternativeServiceType.BAILIFF, true, "gwasanaeth beili"),
            Arguments.of(AlternativeServiceType.ALTERNATIVE_SERVICE, false, "alternative service"),
            Arguments.of(AlternativeServiceType.ALTERNATIVE_SERVICE, true, "cyflwyno amgen")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void shouldSetCorrectServiceApplicationName(
        AlternativeServiceType type,
        boolean isWelsh,
        String expectedName
    ) {
        String serviceApplicationName = ServiceApplicationRejectedNotification.getServiceApplicationName(type, isWelsh);
        assertThat(serviceApplicationName).isEqualTo(expectedName);
    }
}
