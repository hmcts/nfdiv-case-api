package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.PaperApplicationReceivedTemplateContent;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PAPER_APPLICATION_RECEIVED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived.PAPER_APPLICATION_RECEIVED_CONFIDENTIAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.PAPER_APPLICATION_RECEIVED_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaperApplicationReceivedNotification implements ApplicantNotification {

    private final BulkPrintService bulkPrintService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final PaperApplicationReceivedTemplateContent templateContent;

    public static final String LETTER_TYPE_PAPER_APPLICATION_RECEIVED = "paper-application-received";
    public static final String APPLICANT1_DOCUMENT_NAME = "Paper Application Received Letter - Applicant/Applicant1";
    public static final String APPLICANT2_DOCUMENT_NAME = "Paper Application Received Letter - Applicant2";

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending application received letter to applicant 1 for case : {}", caseId);
        generateApplicationReceivedLetterAndSend(caseData, caseId, caseData.getApplicant1());
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending application received letter to applicant 2 for case : {}", caseId);
            generateApplicationReceivedLetterAndSend(caseData, caseId, caseData.getApplicant2());
        }
    }

    private void generateApplicationReceivedLetterAndSend(CaseData caseData, Long caseId, Applicant applicant) {

        Document generatedDocument = generateDocument(caseId, applicant, caseData);

        updateCaseDataWithDocument(caseData,generatedDocument, applicant);

        Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
            List.of(letter),
            caseIdString,
            caseIdString,
            LETTER_TYPE_PAPER_APPLICATION_RECEIVED,
            applicant.getFullName(),
            applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {

        boolean isApplicant1 = applicant.equals(caseData.getApplicant1());
        String documentName = isApplicant1 ? APPLICANT1_DOCUMENT_NAME : APPLICANT2_DOCUMENT_NAME;
        return caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            PAPER_APPLICATION_RECEIVED_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            documentName);
    }

    private void updateCaseDataWithDocument(final CaseData caseData, final Document document, final Applicant applicant) {
        if (applicant.isConfidentialContactDetails()) {
            ConfidentialDivorceDocument divorceDocument = ConfidentialDivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName(document.getFilename())
                .confidentialDocumentsReceived(PAPER_APPLICATION_RECEIVED_CONFIDENTIAL_LETTER)
                .build();
            caseData.getDocuments().setConfidentialDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getConfidentialDocumentsGenerated(), divorceDocument));
        } else {
            DivorceDocument divorceDocument = DivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName(document.getFilename())
                .documentType(PAPER_APPLICATION_RECEIVED_LETTER)
                .build();

            caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getDocumentsGenerated(), divorceDocument));
        }
    }
}
