package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftApplicationRemovalService {

    private final DocumentRemovalService documentRemovalService;

    public List<ListValue<DivorceDocument>> removeDraftApplicationDocument(final List<ListValue<DivorceDocument>> generatedDocuments,
                                                                           final Long caseId) {

        if (isEmpty(generatedDocuments)) {
            log.info("Generated documents list is empty for case id {} ", caseId);
            return emptyList();
        }

        final List<ListValue<DivorceDocument>> applicationDocumentsForRemoval = generatedDocuments
            .stream()
            .filter(this::isApplicationDocument)
            .toList();

        if (!isEmpty(applicationDocumentsForRemoval)) {
            documentRemovalService.deleteDocument(applicationDocumentsForRemoval);
            log.info("Successfully removed application document from case data generated document list for case id {} ", caseId);
        } else {
            log.info("No draft application document found for case id {} ", caseId);
        }

        return generatedDocuments.stream()
            .filter(document -> !isApplicationDocument(document))
            .toList();
    }

    private boolean isApplicationDocument(ListValue<DivorceDocument> document) {
        return document.getValue().getDocumentType().equals(APPLICATION);
    }
}
