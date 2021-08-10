package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.CaseworkerUploadedDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Stream.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;

public final class DocumentUtil {

    private DocumentUtil() {
    }

    public static Document documentFrom(final DocumentInfo documentInfo) {
        return new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl());
    }

    public static DivorceDocument divorceDocumentFrom(final DocumentInfo documentInfo, final DocumentType documentType) {
        return DivorceDocument
            .builder()
            .documentLink(documentFrom(documentInfo))
            .documentFileName(documentInfo.getFilename())
            .documentType(documentType)
            .build();
    }

    public static List<Letter> lettersOfDocumentTypes(final List<ListValue<DivorceDocument>> documents,
                                                      final List<DocumentType> documentTypes) {

        final AtomicInteger letterIndex = new AtomicInteger();
        final Predicate<DivorceDocument> documentTypeFilter =
            document -> documentTypes.contains(document.getDocumentType());

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(documentTypeFilter)
            .map(divorceDocument -> new Letter(divorceDocument, letterIndex.incrementAndGet()))
            .collect(Collectors.toList());
    }


    public static List<ListValue<CaseworkerUploadedDocument>> sortDocumentsInDescendingOrder(
        List<ListValue<CaseworkerUploadedDocument>> previousDocuments,
        List<ListValue<CaseworkerUploadedDocument>> latestDocuments
    ) {

        if (isEmpty(previousDocuments)) {
            return latestDocuments;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(Collectors.toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<CaseworkerUploadedDocument>>> documentsWithoutIds = latestDocuments.stream()
            .collect(Collectors.groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        List<ListValue<CaseworkerUploadedDocument>> newlyAddedDocuments = documentsWithoutIds.get(true);
        List<ListValue<CaseworkerUploadedDocument>> existingDocuments = documentsWithoutIds.get(false);

        AtomicInteger listValueIndex = new AtomicInteger(0);
        newlyAddedDocuments.forEach(
            uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(listValueIndex.incrementAndGet()))
        );

        List<ListValue<CaseworkerUploadedDocument>> sortedDocuments = new ArrayList<>();
        sortedDocuments.addAll(0, newlyAddedDocuments); // add new documents to start of the list

        existingDocuments.forEach(
            uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(listValueIndex.incrementAndGet()))
        );
        sortedDocuments.addAll(1, existingDocuments);

        return sortedDocuments;
    }
}
