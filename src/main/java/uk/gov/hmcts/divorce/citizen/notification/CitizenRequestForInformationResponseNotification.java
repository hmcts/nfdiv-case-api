package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.RequestForInformationResponseDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLS_RESPONSE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitizenRequestForInformationResponseNotification implements ApplicantNotification {

    public static final String REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Response Notification to {} for case id: {}";

    public static final String REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Response Offline Notification to {} for case id: {}";

    public static final String SKIP_REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID =
        "Skipping Request For Information Response Offline Notification to {} for case id: {}. Requested Documents Not Provided.";

    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_SOLICITOR = "applicant solicitor";
    private static final String APPLICANT_1 = "applicant 1";
    private static final String APPLICANT_1_SOLICITOR = "applicant 1 solicitor";
    private static final String APPLICANT_2 = "applicant 2";
    private static final String APPLICANT_2_SOLICITOR = "applicant 2 solicitor";

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final RequestForInformationResponseDocumentPack requestForInformationResponseDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_TO_FOR_CASE_ID,
            caseData.getApplicationType().isSole() ? APPLICANT : APPLICANT_1, caseId);

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
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, caseData.getApplicationType().isSole() ? APPLICANT : APPLICANT_1)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID,
                caseData.getApplicationType().isSole() ? APPLICANT : APPLICANT_1, caseId);

            Applicant applicant1 = caseData.getApplicant1();
            var documentPackInfo = requestForInformationResponseDocumentPack.getDocumentPack(caseData, applicant1);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant1,
                documentPackInfo,
                requestForInformationResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {

        String party = caseData.getApplicationType().isSole() ? APPLICANT_SOLICITOR : APPLICANT_1_SOLICITOR;
        if (allDocsProvided(
                caseData,
                caseId,
                party
        )) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_TO_FOR_CASE_ID,
                    party, caseId);

            Applicant applicant = caseData.getApplicant1();

            notificationService.sendEmail(
                applicant.getSolicitor().getEmail(),
                REQUEST_FOR_INFORMATION_SOLS_RESPONSE,
                applicantTemplateContent(
                    caseData,
                    caseId,
                    applicant,
                    caseData.getApplicant2()
                ),
                applicant.getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant1SolicitorOffline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(
            caseData,
            caseId,
            caseData.getApplicationType().isSole() ? APPLICANT_SOLICITOR : APPLICANT_1_SOLICITOR)
        ) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID,
                caseData.getApplicationType().isSole() ? APPLICANT_SOLICITOR : APPLICANT_1_SOLICITOR, caseId);

            Applicant applicant1 = caseData.getApplicant1();
            var documentPackInfo = requestForInformationResponseDocumentPack.getDocumentPack(caseData, applicant1);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant1,
                documentPackInfo,
                requestForInformationResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2, caseId);

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
        if (allDocsProvided(caseData, caseId, APPLICANT_2_SOLICITOR)) {

            log.info(REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_TO_FOR_CASE_ID,
                APPLICANT_2_SOLICITOR, caseId);

            Applicant applicant2 = caseData.getApplicant2();

            notificationService.sendEmail(
                    applicant2.getSolicitor().getEmail(),
                    REQUEST_FOR_INFORMATION_SOLS_RESPONSE,
                    applicantTemplateContent(
                            caseData,
                            caseId,
                            applicant2,
                            caseData.getApplicant1()
                    ),
                    applicant2.getLanguagePreference(),
                    caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId,  APPLICANT_2)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2, caseId);

            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = requestForInformationResponseDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant2,
                documentPackInfo,
                requestForInformationResponseDocumentPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant2SolicitorOffline(final CaseData caseData, final Long caseId) {
        if (allDocsProvided(caseData, caseId, APPLICANT_2_SOLICITOR)) {
            log.info(REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, APPLICANT_2_SOLICITOR, caseId);

            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = requestForInformationResponseDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(
                caseData,
                caseId,
                applicant2,
                documentPackInfo,
                requestForInformationResponseDocumentPack.getLetterId()
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

    private EmailTemplateName getEmailTemplateName(RequestForInformationResponse requestForInformationResponse) {
        if (requestForInformationResponse.isOffline()) {
            return YES.equals(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded())
                ? REQUEST_FOR_INFORMATION_RESPONSE
                : REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
        } else {
            return YES.equals(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs())
                ? REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS
                : REQUEST_FOR_INFORMATION_RESPONSE;
        }
    }

    private boolean allDocsProvided(final CaseData caseData, final Long caseId, final String errorText) {
        RequestForInformationResponse latestResponse = caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();
        if (latestResponse.areAllDocsUploaded()) {
            return true;
        }
        log.info(SKIP_REQUEST_FOR_INFORMATION_RESPONSE_OFFLINE_NOTIFICATION_TO_FOR_CASE_ID, errorText, caseId);
        return false;
    }
}
