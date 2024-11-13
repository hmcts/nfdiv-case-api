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

import static java.lang.String.join;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SMART_SURVEY_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addCannotUploadResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationTemplateVars;

@ExtendWith(MockitoExtension.class)
class CitizenRequestForInformationResponsePartnerNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant1() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_PARTNER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant1Solicitor() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, true, false).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1Solicitor() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, true, false).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant2());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant1AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT2);

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_PARTNER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant1SolicitorAfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, true, false).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT2);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT2
        );

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant1SolicitorAfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, true, false).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT2
        );

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant2() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_PARTNER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant2Solicitor() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, true).getData();
        addResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2Solicitor() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, true).getData();
        addCannotUploadResponseToLatestRequestForInformation(caseData, caseData.getApplicant1());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant2AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT1);

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_PARTNER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponsePartnerEmailToApplicant2SolicitorAfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, true).getData();
        addOfflineResponseToLatestRequestForInformation(caseData, RequestForInformationOfflineResponseJointParties.APPLICANT1);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2AfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT1
        );

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationResponseCannotUploadDocsEmailToApplicant2SolicitorAfterOfflineResponse() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, true).getData();
        addNotAllDocsUploadedOfflineResponseToLatestRequestForInformation(
            caseData,
            RequestForInformationOfflineResponseJointParties.APPLICANT1
        );

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = solicitorTemplateContent();

        citizenRequestForInformationResponsePartnerNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD,
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

    private Map<String, String> solicitorTemplateContent() {
        Map<String, String> templateVars = getMainTemplateVars();

        templateVars.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(RESPONDENT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(DATE_OF_ISSUE, "");
        templateVars.put(SOLICITOR_REFERENCE, "not provided");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL);
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }
}
