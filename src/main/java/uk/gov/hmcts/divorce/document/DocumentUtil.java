package uk.gov.hmcts.divorce.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

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

    public static ConfidentialDivorceDocument divorceDocumentFrom(final DocumentInfo documentInfo,
                                                                  final ConfidentialDocumentsReceived documentType) {
        return ConfidentialDivorceDocument
            .builder()
            .documentLink(documentFrom(documentInfo))
            .documentFileName(documentInfo.getFilename())
            .confidentialDocumentsReceived(documentType)
            .build();
    }

    public static List<Letter> lettersWithDocumentType(final List<ListValue<DivorceDocument>> documents,
                                                       final DocumentType documentType) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(document -> documentType.equals(document.getDocumentType()))
            .map(divorceDocument -> new Letter(divorceDocument, null, letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static List<Letter> mapToLetters(final List<ListValue<Document>> documents, final DocumentType documentType) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .map(document -> new Letter(DivorceDocument.builder()
                .documentType(documentType)
                .documentFileName(document.getFilename())
                .documentLink(document)
                .build(), null, letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static List<Letter> lettersWithAosScannedDocument(final List<ListValue<ScannedDocument>> documents) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(document -> "aos".equals(document.getSubtype()))
            .map(scannedDocument -> new Letter(null, scannedDocument, letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static boolean isApplicableForConfidentiality(final DocumentType documentType) {
        List<DocumentType> documentsApplicableForConfidentiality = Lists.newArrayList(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDINGS_APP_2
        );

        return documentsApplicableForConfidentiality.contains(documentType);
    }
}
