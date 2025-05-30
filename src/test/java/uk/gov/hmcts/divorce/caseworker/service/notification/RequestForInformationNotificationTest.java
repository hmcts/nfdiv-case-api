package uk.gov.hmcts.divorce.caseworker.service.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.RequestForInformationDocumentPack;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.join;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_YET_ISSUED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.REQUEST_FOR_INFORMATION_DETAILS;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SENT_TO_BOTH_APPLICANTS;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_JOINT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_OTHER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SMART_SURVEY_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationOtherCaseDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationTemplateVars;

@ExtendWith(MockitoExtension.class)
class RequestForInformationNotificationTest {

    private static final String LETTER_TYPE_REQUEST_FOR_INFORMATION = "request-for-information-letter";

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RequestForInformationDocumentPack requestForInformationDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private RequestForInformationNotification requestForInformationNotification;

    @Test
    void shouldSendRequestForInformationEmailToApplicantWhenNotRepresentedOnSoleCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT, false, false).getData();

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        requestForInformationNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_SOLE,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationLetterToOfflineApplicant() {
        CaseData caseData = caseData();

        when(requestForInformationDocumentPack.getDocumentPack(any(), any())).thenReturn(getApplicantDocumentPack());
        when(requestForInformationDocumentPack.getLetterId()).thenReturn(LETTER_TYPE_REQUEST_FOR_INFORMATION);

        requestForInformationNotification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            getApplicantDocumentPack(),
            LETTER_TYPE_REQUEST_FOR_INFORMATION
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToApplicantSolicitorWhenRepresentedOnSoleCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT, true, false).getData();
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);
        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getSolicitorTemplateContent(caseData);
        templateContent.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(ISSUE_DATE_POPULATED, YES);
        templateContent.put(NOT_YET_ISSUED, NO);
        templateContent.put(SOLICITOR_REFERENCE, TEST_REFERENCE);

        requestForInformationNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationLetterToOfflineApplicantSolicitor() {
        CaseData caseData = caseData();

        when(requestForInformationDocumentPack.getDocumentPack(any(), any())).thenReturn(getSolicitorDocumentPack());
        when(requestForInformationDocumentPack.getLetterId()).thenReturn(LETTER_TYPE_REQUEST_FOR_INFORMATION);

        requestForInformationNotification.sendToApplicant1SolicitorOffline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            getSolicitorDocumentPack(),
            LETTER_TYPE_REQUEST_FOR_INFORMATION
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToApplicant1WhenNotRepresentedOnJointCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, false, false).getData();

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        requestForInformationNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_JOINT,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationEmailWithoutSuppressedJointDataTemplateTextWhenNotRepresentedOnJointCaseAndSentToBothApplicants() {
        CaseData caseData = getRequestForInformationCaseDetails(BOTH, false, false).getData();

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getRequestForInformationTemplateVars(
                caseData.getApplicationType(), BOTH, caseData.isDivorce(), caseData.getApplicant2()
            ));

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();
        templateContent.put(IS_JOINT, YES);
        templateContent.put(SENT_TO_BOTH_APPLICANTS, YES);
        templateContent.put(HUSBAND_JOINT, YES);

        requestForInformationNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_JOINT,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToApplicant1SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT1, true, false).getData();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);
        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getSolicitorTemplateContent(caseData);

        requestForInformationNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToApplicant2WhenNotRepresentedOnJointCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, false).getData();

        when(commonContent.requestForInformationTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getRequestForInformationTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getApplicantTemplateContent();

        requestForInformationNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            REQUEST_FOR_INFORMATION_JOINT,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationLetterToOfflineApplicant2() {
        CaseData caseData = caseData();

        when(requestForInformationDocumentPack.getDocumentPack(any(), any())).thenReturn(getApplicantDocumentPack());
        when(requestForInformationDocumentPack.getLetterId()).thenReturn(LETTER_TYPE_REQUEST_FOR_INFORMATION);

        requestForInformationNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            getApplicantDocumentPack(),
            LETTER_TYPE_REQUEST_FOR_INFORMATION
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToApplicant2SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = getRequestForInformationCaseDetails(APPLICANT2, false, true).getData();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);
        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getSolicitorTemplateContent(caseData);

        requestForInformationNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            REQUEST_FOR_INFORMATION_SOLICITOR,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationLetterToOfflineApplicant2Solicitor() {
        CaseData caseData = caseData();

        when(requestForInformationDocumentPack.getDocumentPack(any(), any())).thenReturn(getSolicitorDocumentPack());
        when(requestForInformationDocumentPack.getLetterId()).thenReturn(LETTER_TYPE_REQUEST_FOR_INFORMATION);

        requestForInformationNotification.sendToApplicant2SolicitorOffline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            getSolicitorDocumentPack(),
            LETTER_TYPE_REQUEST_FOR_INFORMATION
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToOtherRecipientOnSoleCase() {
        CaseData caseData = getRequestForInformationOtherCaseDetails(SOLE_APPLICATION, false, false).getData();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getOtherRecipientTemplateContent(caseData);

        requestForInformationNotification.sendToOtherRecipient(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_OTHER_EMAIL,
            REQUEST_FOR_INFORMATION_OTHER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendRequestForInformationEmailToOtherRecipientOnJointCase() {
        CaseData caseData = getRequestForInformationOtherCaseDetails(JOINT_APPLICATION, false, false).getData();
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        when(commonContent.getSmartSurvey()).thenReturn(SMART_SURVEY_TEST_URL);

        Map<String, String> templateContent = getOtherRecipientTemplateContent(caseData);
        templateContent.put(ISSUE_DATE_POPULATED, YES);
        templateContent.put(NOT_YET_ISSUED, NO);

        requestForInformationNotification.sendToOtherRecipient(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_OTHER_EMAIL,
            REQUEST_FOR_INFORMATION_OTHER,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    private Map<String, String> getApplicantTemplateContent() {
        Map<String, String> templateVars = getRequestForInformationTemplateVars();
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT);
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }

    private Map<String, String> getSolicitorTemplateContent(final CaseData caseData) {
        Map<String, String> templateVars = getMainTemplateVars();

        templateVars.put(APPLICANT_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(DATE_OF_ISSUE, "");
        templateVars.put(ISSUE_DATE_POPULATED, NO);
        templateVars.put(NOT_YET_ISSUED, YES);
        templateVars.put(SOLICITOR_REFERENCE, "not provided");
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT);
        templateVars.put(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL);
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }

    private Map<String, String> getOtherRecipientTemplateContent(final CaseData caseData) {
        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(APPLICANT_NAME, caseData.getApplicant1().getFullName());
        templateVars.put(RESPONDENT_NAME, caseData.getApplicant2().getFullName());
        templateVars.put(RECIPIENT_NAME, TEST_OTHER_NAME);
        templateVars.put(ISSUE_DATE_POPULATED, NO);
        templateVars.put(NOT_YET_ISSUED, YES);
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT);
        templateVars.put(SMART_SURVEY, SMART_SURVEY_TEST_URL);

        return templateVars;
    }

    private DocumentPackInfo getApplicantDocumentPack() {
        return new DocumentPackInfo(
            ImmutableMap.of(
                REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID)
            ),
            ImmutableMap.of(
                REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME
            )
        );
    }

    private DocumentPackInfo getSolicitorDocumentPack() {
        return new DocumentPackInfo(
            ImmutableMap.of(
                REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID)
            ),
            ImmutableMap.of(
                REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME
            )
        );
    }
}
