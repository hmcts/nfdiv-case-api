package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffServiceSuccessfulTemplateContent;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived.BAILIFF_SERVICE_SUCCESSFUL_CONFIDENTIAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_SUCCESSFUL_LETTER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_SUCCESSFUL;

@Component
@Slf4j
@RequiredArgsConstructor
public class BailiffServiceSuccessfulNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final  BailiffServiceSuccessfulTemplateContent templateContent;

    private final CommonContent commonContent;

    public static final String BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME = "Bailiff Service Successful Letter";
    public static final String BAILIFF_SERVICE_SUCCESSFUL_LETTER_ID = "bailiff-service-success-letter";

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant that Bailiff service was successful");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            BAILIFF_SERVICE_SUCCESSFUL,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        if (!caseData.isJudicialSeparationCase()) {
            log.info("Sending bailiff service successful letter to applicant : {}", caseId);
            generateLetterAndSend(caseData, caseId, caseData.getApplicant1());
        }
    }

    private void generateLetterAndSend(CaseData caseData, Long caseId, Applicant applicant) {

        Document generatedDocument = caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME);

        updateCaseDataWithDocument(caseData,generatedDocument, applicant);

        Letter bailiffServiceSuccessletter = new  Letter(generatedDocument, 1);
        Letter certificateOfService = new Letter(
            caseData.getAlternativeService().getBailiff().getCertificateOfServiceDocument().getDocumentLink(),
            1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
            List.of(bailiffServiceSuccessletter, certificateOfService),
            caseIdString,
            caseIdString,
            BAILIFF_SERVICE_SUCCESSFUL_LETTER_ID,
            applicant.getFullName(),
            applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {
        return caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            BAILIFF_SERVICE_SUCCESSFUL_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            BAILIFF_SERVICE_SUCCESSFUL_DOCUMENT_NAME);
    }

    private void updateCaseDataWithDocument(final CaseData caseData, final Document document, final Applicant applicant) {
        if (applicant.isConfidentialContactDetails()) {
            ConfidentialDivorceDocument divorceDocument = ConfidentialDivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName(document.getFilename())
                .confidentialDocumentsReceived(BAILIFF_SERVICE_SUCCESSFUL_CONFIDENTIAL_LETTER)
                .build();
            caseData.getDocuments().setConfidentialDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getConfidentialDocumentsGenerated(), divorceDocument));
        } else {
            DivorceDocument divorceDocument = DivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName(document.getFilename())
                .documentType(BAILIFF_SERVICE_SUCCESSFUL_LETTER)
                .build();

            caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getDocumentsGenerated(), divorceDocument));
        }
    }
}
