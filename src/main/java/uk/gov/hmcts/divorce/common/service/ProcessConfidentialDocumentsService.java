package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isApplicableForConfidentiality;

@Service
@Slf4j
public class ProcessConfidentialDocumentsService {

    public void processDocuments(final CaseData caseData, final Long caseId) {

        log.info("Processing confidential documents for case id : {}", caseId);

        processDocuments(caseData, caseData.getApplicant1(), true);

        processDocuments(caseData, caseData.getApplicant2(), false);
    }

    public void processDocuments(final CaseData caseData, final Applicant applicant,
                                                         final boolean isApplicant1) {
        if (applicant.isConfidentialContactDetails()) {
            moveToConfidentialDocumentsTab(caseData.getDocuments(), isApplicant1);
        }
    }

    private void moveToConfidentialDocumentsTab(final CaseDocuments caseDocuments, final Boolean isApplicant1) {
        log.info("Moving documents to confidential list for : applicant {}", isApplicant1 ? "1" : "2");

        List<ListValue<DivorceDocument>> documentsToMove
            = ofNullable(caseDocuments.getDocumentsGenerated())
            .flatMap(Collection::stream)
            .filter(document -> isApplicableForConfidentiality(document.getValue().getDocumentType(), isApplicant1))
            .toList();

        if (!CollectionUtils.isEmpty(documentsToMove)) {

            documentsToMove.forEach(documentListValue ->
                caseDocuments.setConfidentialDocumentsGenerated(addDocumentToTop(
                    caseDocuments.getConfidentialDocumentsGenerated(),
                    mapToConfidentialDivorceDocument(documentListValue.getValue(), isApplicant1),
                    documentListValue.getId()
                )));

            caseDocuments.getDocumentsGenerated().removeAll(documentsToMove);
        }
    }

    private ConfidentialDivorceDocument mapToConfidentialDivorceDocument(final DivorceDocument divorceDocument,
                                                                         final boolean isApplicant1) {
        return ConfidentialDivorceDocument.builder()
            .documentLink(divorceDocument.getDocumentLink())
            .documentComment(divorceDocument.getDocumentComment())
            .documentFileName(divorceDocument.getDocumentFileName())
            .documentDateAdded(divorceDocument.getDocumentDateAdded())
            .documentEmailContent(divorceDocument.getDocumentEmailContent())
            .confidentialDocumentsReceived(isApplicant1 ? ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1
                : ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
            .build();
    }
}
