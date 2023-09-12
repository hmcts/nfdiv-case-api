package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;

@Service
@Slf4j
public class ProcessConfidentialDocumentsService {

    public void processDocuments(final CaseData caseData, final Long caseId) {

        log.info("Processing confidential documents for case id : {}", caseId);
        if (caseData.getApplicant1().isConfidentialContactDetails()) {
            moveToConfidentialDocumentsTab(caseData, caseId, true);
        }

        if (caseData.getApplicant2().isConfidentialContactDetails()) {
            moveToConfidentialDocumentsTab(caseData, caseId, false);
        }
    }

    private void moveToConfidentialDocumentsTab(final CaseData caseData, final Long caseId, boolean isApplicant1) {
        log.info("Moving documents to confidential list for case {}", caseId);

        CaseDocuments caseDocuments = caseData.getDocuments();

        List<ListValue<DivorceDocument>> documentsToMove
            = ofNullable(caseDocuments.getDocumentsGenerated())
            .flatMap(Collection::stream)
            .filter(document -> filterDocumentBasedOnPotentialConfidentiality(document.getValue(), caseData, isApplicant1))
            .toList();

        if (!CollectionUtils.isEmpty(documentsToMove)) {

            documentsToMove.forEach(documentListValue ->
                caseDocuments.setConfidentialDocumentsGenerated(addDocumentToTop(
                    caseDocuments.getConfidentialDocumentsGenerated(),
                    mapToConfidentialDivorceDocument(documentListValue.getValue()),
                    documentListValue.getId()
                )));

            caseDocuments.getDocumentsGenerated().removeAll(documentsToMove);
        }
    }

    private ConfidentialDivorceDocument mapToConfidentialDivorceDocument(final DivorceDocument divorceDocument) {
        return ConfidentialDivorceDocument.builder()
            .documentLink(divorceDocument.getDocumentLink())
            .documentComment(divorceDocument.getDocumentComment())
            .documentFileName(divorceDocument.getDocumentFileName())
            .documentDateAdded(divorceDocument.getDocumentDateAdded())
            .documentEmailContent(divorceDocument.getDocumentEmailContent())
            .confidentialDocumentsReceived(getConfidentialDocumentType(divorceDocument.getDocumentType()))
            .build();
    }

    private boolean filterDocumentBasedOnPotentialConfidentiality(final DivorceDocument document,
                                                                  final CaseData caseData,
                                                                  boolean isApplicant1) {
        if (DocumentType.GENERAL_LETTER.equals(document.getDocumentType())) {
            return isConfidentialGeneralLetter(caseData, document, isApplicant1);
        }

        return isConfidential(caseData, document.getDocumentType());
    }

    private boolean isConfidentialGeneralLetter(final CaseData caseData,
                                                final DivorceDocument generalLetterDocument,
                                                boolean isApplicant1) {

        GeneralParties party = isApplicant1 ? APPLICANT : RESPONDENT;

        return ofNullable(caseData.getGeneralLetters())
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .anyMatch(generalLetterDetail -> party.equals(generalLetterDetail.getGeneralLetterParties())
                && generalLetterDetail.getGeneralLetterLink().getUrl().equals(generalLetterDocument.getDocumentLink().getUrl()));
    }

}
