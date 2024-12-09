package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseJointParties;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SMART_SURVEY_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addCannotUploadResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationTemplateVars;

@ExtendWith(MockitoExtension.class)
class CitizenRequestForInformationResponseNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CitizenRequestForInformationResponseNotification citizenRequestForInformationResponseNotification;

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant1() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant1AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT1);

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT1
        );

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant2() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant2AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT2);

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT2
        );

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponseNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    private Map<String, String> getApplicantTemplateContent() {
        Map<String, String> templateVars = getRequestForInformationTemplateVars();
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }
}
