package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Component
@Slf4j
public class GeneralEmailNotification {

    public static final String GENERAL_EMAIL_DETAILS = "general email details";
    public static final String GENERAL_OTHER_RECIPIENT_NAME = "general other recipient name";
    private static final String DOCUMENTS_AVAILABLE = "areDocuments";

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
        List<ListValue<Document>> documents = new ArrayList<>();

        GeneralEmail generalEmail = caseData.getGeneralEmail();

        if (generalEmail == null) {
            return;
        }
        if (!CollectionUtils.isEmpty(generalEmail.getGeneralEmailAttachments())) {

            templateVars.put(DOCUMENTS_AVAILABLE,"yes");

            documents = ofNullable(generalEmail.getGeneralEmailAttachments())
                .flatMap(Collection::stream)
                .map(divorceDocument -> ListValue.<Document>builder()
                    .id(divorceDocument.getId())
                    .value(divorceDocument.getValue().getDocumentLink()).build())
                .toList();
        }

        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, generalEmail.getGeneralEmailOtherRecipientName());
        templateVars.put(GENERAL_EMAIL_DETAILS, generalEmail.getGeneralEmailDetails());

        GeneralParties parties = generalEmail.getGeneralEmailParties();

        if (APPLICANT.equals(parties)) {
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
        } else if (RESPONDENT.equals(parties)) {
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
            emailTo = generalEmail.getGeneralEmailOtherRecipientEmail();
            templateId = GENERAL_EMAIL_OTHER_PARTY;
        }

        Map<String, Object> templateVarsObj = addAttachmentsToTemplateVars(documents, templateVars);

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

    private Map<String, Object> addAttachmentsToTemplateVars(List<ListValue<Document>> documents,
                                                             Map<String,String> vars) throws NotificationClientException, IOException {
        Map<String, Object> templateVarsObj = new HashMap<>(vars);

        int documentId = 0;
        for (ListValue<Document> document : documents) {
            ++documentId;
            try {
                byte[] sotDocument = getDocumentBytes(document.getValue());
                templateVarsObj.put(String.format("sot%s", documentId), prepareUpload(sotDocument));
            } catch (NotificationClientException e) {
                throw new NotificationClientException("Size exceeds 2MB for file : " + document.getValue().getFilename());
            }
        }

        return templateVarsObj;
    }

    private Map<String, String> templateVars(final CaseData caseData, final Long caseId) {
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);
        templateVars.put("sot1", "");
        templateVars.put("sot2", "");
        templateVars.put("sot3", "");
        templateVars.put("sot4", "");
        templateVars.put("sot5", "");
        templateVars.put("sot6", "");
        templateVars.put("sot7", "");
        templateVars.put("sot8", "");
        templateVars.put("sot9", "");
        templateVars.put("sot10", "");
        templateVars.put(DOCUMENTS_AVAILABLE,"no");
        return templateVars;
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
