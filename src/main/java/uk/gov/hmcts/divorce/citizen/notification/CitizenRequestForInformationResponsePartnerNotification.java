package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.RequestForInformationPartnerResponseDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
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
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitizenRequestForInformationResponsePartnerNotification implements ApplicantNotification {

    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Response Partner Notification to {} for case id: {}";

    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Response Partner Offline Notification to {} for case id: {}";

    public static final String SKIP_REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID =
        "Skipping Request For Information Response Partner Offline Notification to {} for case id: {}. Requested Documents Not Provided.";
    private static final String APPLICANT_1 = "applicant 1";
    private static final String APPLICANT_1_SOLICITOR = "applicant 1 solicitor";
    private static final String APPLICANT_2 = "applicant 2";
    private static final String APPLICANT_2_SOLICITOR = "applicant 2 solicitor";

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final RequestForInformationPartnerResponseDocumentPack requestForInformationPartnerResponseDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_1, caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            getEmailTemplateName(latestResponse),
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_1_SOLICITOR, caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            getSolicitorEmailTemplateName(latestResponse),
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, caseData.getApplicationType().isSole() ? "applicant" : APPLICANT_1)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID,
                caseData.getApplicationType().isSole() ? "applicant" : APPLICANT_1, caseId);

            Applicant applicant1 = caseData.getApplicant1();
            var documentPackInfo = requestForInformationPartnerResponseDocumentPack.getDocumentPack(caseData, applicant1);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant1,
                documentPackInfo,
                requestForInformationPartnerResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant1SolicitorOffline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, caseData.getApplicationType().isSole() ? "applicant solicitor" : APPLICANT_1_SOLICITOR)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID,
                caseData.getApplicationType().isSole() ? "applicant solicitor" : APPLICANT_1_SOLICITOR, caseId);

            Applicant applicant1 = caseData.getApplicant1();
            var documentPackInfo = requestForInformationPartnerResponseDocumentPack.getDocumentPack(caseData, applicant1);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant1,
                documentPackInfo,
                requestForInformationPartnerResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2, caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            getEmailTemplateName(latestResponse),
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2_SOLICITOR, caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            getSolicitorEmailTemplateName(latestResponse),
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, APPLICANT_2)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2, caseId);

            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = requestForInformationPartnerResponseDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant2,
                documentPackInfo,
                requestForInformationPartnerResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant2SolicitorOffline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, APPLICANT_2_SOLICITOR)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2_SOLICITOR, caseId);

            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = requestForInformationPartnerResponseDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant2,
                documentPackInfo,
                requestForInformationPartnerResponseDocumentPack.getLetterId()
            );
        }
    }

    private Map<String, String> applicantTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars =
            commonContent.requestForInformationTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private Map<String, String> solicitorTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        LocalDate issueDate = caseData.getApplication().getIssueDate();

        templateVars.put(APPLICANT_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateVars.put(DATE_OF_ISSUE, issueDate != null ? issueDate.format(DATE_TIME_FORMATTER) : "");
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant.getSolicitor().getReference())
            ? applicant.getSolicitor().getReference()
            : "not provided");
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private EmailTemplateName getEmailTemplateName(RequestForInformationResponse requestForInformationResponse) {
        if (requestForInformationResponse.isOffline()) {
            return YES.equals(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded())
                ? REQUEST_FOR_INFORMATION_RESPONSE_PARTNER
                : REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
        } else {
            return YES.equals(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs())
                ? REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS
                : REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;
        }
    }

    private EmailTemplateName getSolicitorEmailTemplateName(RequestForInformationResponse requestForInformationResponse) {
        if (requestForInformationResponse.isOffline()) {
            return YES.equals(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded())
                ? REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY
                : REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD;
        } else {
            return YES.equals(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs())
                ? REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD
                : REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY;
        }
    }

    private boolean allDocsProvided(final CaseData caseData, final Long caseId, final String errorText) {
        RequestForInformationResponse latestResponse = caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();
        if (latestResponse.areAllDocsUploaded()) {
            return true;
        }
        log.info(SKIP_REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, errorText, caseId);
        return false;
    }
}
