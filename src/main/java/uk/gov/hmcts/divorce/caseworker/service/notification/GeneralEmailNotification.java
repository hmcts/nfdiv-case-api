package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Component
@Slf4j
public class GeneralEmailNotification {

    public static final String GENERAL_EMAIL_DETAILS = "general email details";
    public static final String GENERAL_OTHER_RECIPIENT_NAME = "general other recipient name";
    private static final String PERSONALISATION_SOT_LINK = "sot_link";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CaseDocumentAccessManagement documentManagementClient;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void send(final CaseData caseData, final Long caseId) throws NotificationClientException, IOException {
        log.info("Sending General Email Notification for case id: {}", caseId);

        String emailTo = null;
        EmailTemplateName templateId;

        Map<String, String> templateVars = templateVars(caseData, caseId);

        if (APPLICANT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
            if (caseData.getApplicant1().isRepresented()) {
                log.info("Sending General Email Notification to petitioner solicitor for case id: {}", caseId);
                emailTo = caseData.getApplicant1().getSolicitor().getEmail();
                templateId = GENERAL_EMAIL_PETITIONER_SOLICITOR;
                templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
            } else {
                log.info("Sending General Email Notification to petitioner for case id: {}", caseId);
                emailTo = caseData.getApplicant1().getEmail();
                templateId = GENERAL_EMAIL_PETITIONER;
            }
        } else if (RESPONDENT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
            if (caseData.getApplicant2().isRepresented()) {
                log.info("Sending General Email Notification to respondent solicitor for case id: {}", caseId);
                emailTo = caseData.getApplicant2().getSolicitor().getEmail();
                templateId = GENERAL_EMAIL_RESPONDENT_SOLICITOR;
                templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
            } else {
                log.info("Sending General Email Notification to respondent for case id: {}", caseId);
                emailTo = caseData.getApplicant2().getEmail();
                templateId = GENERAL_EMAIL_RESPONDENT;
            }
        } else {
            log.info("Sending General Email Notification to other party for case id: {}", caseId);
            emailTo = caseData.getGeneralEmail().getGeneralEmailOtherRecipientEmail();
            templateId = GENERAL_EMAIL_OTHER_PARTY;
        }

        //Code to get attachments and add to personalisation
        //Get Attachment from general email
        Map<String, Object> templateVarsObj = new HashMap<>(templateVars);
        if (!CollectionUtils.isEmpty(caseData.getGeneralEmail().getGeneralEmailAttachments())) {
            Document document = firstElement(caseData.getGeneralEmail().getGeneralEmailAttachments()).getValue().getDocumentLink();
            //Get byte code for document
            byte[] sotDocument = getDocumentBytes(document);
            //Upload document to notify
            templateVarsObj.put(PERSONALISATION_SOT_LINK, prepareUpload(sotDocument));
        }

        if (null == emailTo) {
            log.info("Email address is not available for template id {} and case {} ", templateId, caseId);
        } else {
            notificationService.sendEmail(
                emailTo,
                templateId,
                templateVarsObj,
                ENGLISH,
                caseId
            );
            log.info("Successfully sent general email notification for case id: {}", caseId);
        }
    }

    private Map<String, String> templateVars(final CaseData caseData, final Long caseId) {
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, caseData.getGeneralEmail().getGeneralEmailOtherRecipientName());
        templateVars.put(GENERAL_EMAIL_DETAILS, caseData.getGeneralEmail().getGeneralEmailDetails());
        return templateVars;
    }

    private static JSONObject prepareUpload(byte[] documentContents) throws NotificationClientException {
        if (documentContents.length > 2097152) {
            throw new NotificationClientException("File is larger than 2MB");
        } else {
            byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
            String fileContent = new String(fileContentAsByte, StandardCharsets.ISO_8859_1);
            JSONObject jsonFileObject = new JSONObject();
            jsonFileObject.put("file", fileContent);
            jsonFileObject.put("filename", JSONObject.NULL);
            jsonFileObject.put("confirm_email_before_download", true);
            jsonFileObject.put("retention_period", JSONObject.NULL);
            return jsonFileObject;
        }
    }

    private byte[] getDocumentBytes(Document document) throws IOException, NotificationClientException {
        final String authToken = authTokenGenerator.generate();
        final var systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        final var userAuth = systemUpdateUser.getAuthToken();
        ResponseEntity<Resource> response = documentManagementClient.downloadBinary(userAuth, authToken, document);

        Resource body = response.getBody();
        if (body != null) {
            return IOUtils.toByteArray(body.getInputStream());
        } else {
            throw new NotificationClientException("No body retrieved for document resource: " + document.getUrl());
        }
    }
}
