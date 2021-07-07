package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.print.model.Letter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Stream.ofNullable;

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
}
