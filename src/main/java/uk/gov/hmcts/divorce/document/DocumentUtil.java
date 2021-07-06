package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.print.model.Letter;

import java.util.ArrayList;
import java.util.List;

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

    public static List<DivorceDocument> findDocumentsOfType(final CaseData caseData, final List<DocumentType> documentTypes) {

        final List<DivorceDocument> divorceDocuments = new ArrayList<>();
        final List<ListValue<DivorceDocument>> documentsGenerated = caseData.getDocumentsGenerated();

        if (null != documentsGenerated) {
            for (final ListValue<DivorceDocument> listValue : documentsGenerated) {
                final DivorceDocument divorceDocument = listValue.getValue();

                if (isDocumentTypeOf(divorceDocument, documentTypes)) {
                    divorceDocuments.add(divorceDocument);
                }
            }
        }

        return divorceDocuments;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static List<Letter> convertToLetters(final List<DivorceDocument> divorceDocuments) {

        final List<Letter> letters = new ArrayList<>();
        int counter = 1;

        for (final DivorceDocument divorceDocument : divorceDocuments) {
            letters.add(new Letter(divorceDocument, counter++));
        }

        return letters;
    }

    private static boolean isDocumentTypeOf(final DivorceDocument divorceDocument, final List<DocumentType> documentTypes) {
        return documentTypes.contains(divorceDocument.getDocumentType());
    }
}
