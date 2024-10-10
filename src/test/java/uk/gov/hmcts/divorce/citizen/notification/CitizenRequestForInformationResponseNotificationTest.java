package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.SMART_SURVEY_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
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
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setValues(caseData);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        caseData.getRequestForInformationList().getRequestForInformationResponseApplicant1().setRfiDraftResponseDetails(TEST_TEXT);
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

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
    void shouldSendRequestForInformationResponseEmailToApplicant2() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setValues(caseData);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        caseData.getRequestForInformationList().getRequestForInformationResponseApplicant2().setRfiDraftResponseDetails(TEST_TEXT);
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

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

    private Map<String, String> getApplicantTemplateContent() {
        Map<String, String> templateVars = getRequestForInformationTemplateVars();
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }
}
