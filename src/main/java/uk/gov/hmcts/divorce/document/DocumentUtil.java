package uk.gov.hmcts.divorce.document;

import com.google.common.collect.Lists;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

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

    public static boolean documentsWithDocumentType(final List<ListValue<DivorceDocument>> documents,
                                                    final DocumentType documentType) {

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .anyMatch(document -> documentType.equals(document.getDocumentType()));
    }

    public static List<Letter> lettersWithDocumentType(final List<ListValue<DivorceDocument>> documents,
                                                       final DocumentType documentType) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(document -> documentType.equals(document.getDocumentType()))
            .map(divorceDocument -> new Letter(divorceDocument, letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static List<Letter> lettersWithConfidentialDocumentType(final List<ListValue<ConfidentialDivorceDocument>> documents,
                                                                   final ConfidentialDocumentsReceived documentType) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(document -> documentType.equals(document.getConfidentialDocumentsReceived()))
            .map(confidentialDivorceDocument -> new Letter(confidentialDivorceDocument, letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static List<Letter> lettersWithAosScannedDocument(final List<ListValue<DivorceDocument>> documents) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return ofNullable(documents)
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(document -> RESPONDENT_ANSWERS.equals(document.getDocumentType()))
            .map(scannedDocument -> new Letter(scannedDocument, letterIndex.incrementAndGet()))
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
                .build(), letterIndex.incrementAndGet()))
            .collect(toList());
    }

    public static boolean isApplicableForConfidentiality(final DocumentType documentType, final Boolean isApplicant1) {
        List<DocumentType> documentsForApplicant1 = Lists.newArrayList(NOTICE_OF_PROCEEDINGS_APP_1);

        List<DocumentType> documentsForApplicant2 = Lists.newArrayList(NOTICE_OF_PROCEEDINGS_APP_2);

        List<DocumentType> documentsForBothApplicants = Lists.newArrayList(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDINGS_APP_2,
            GENERAL_LETTER
        );

        return isApplicant1 == null ? documentsForBothApplicants.contains(documentType)
            : isApplicant1 ? documentsForApplicant1.contains(documentType) : documentsForApplicant2.contains(documentType);
    }

    public static boolean isApplicableForConfidentiality(final ConfidentialDocumentsReceived documentType, final Boolean isApplicant1) {
        List<ConfidentialDocumentsReceived> documentsForApplicant1 = Lists.newArrayList(
            ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1
        );

        List<ConfidentialDocumentsReceived> documentsForApplicant2 = Lists.newArrayList(
            ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2
        );

        return isApplicant1 ? documentsForApplicant1.contains(documentType) : documentsForApplicant2.contains(documentType);
    }

    public static boolean isConfidential(final CaseData caseData, final DocumentType documentType) {
        if (NOTICE_OF_PROCEEDINGS_APP_1.equals(documentType)
            || GENERAL_LETTER.equals(documentType) && APPLICANT.equals(caseData.getGeneralLetter().getGeneralLetterParties())) {
            return caseData.getApplicant1().isConfidentialContactDetails();
        } else if (NOTICE_OF_PROCEEDINGS_APP_2.equals(documentType)
            || GENERAL_LETTER.equals(documentType) && RESPONDENT.equals(caseData.getGeneralLetter().getGeneralLetterParties())) {
            return caseData.getApplicant2().isConfidentialContactDetails();
        } else {
            return false;
        }
    }

    public static ConfidentialDocumentsReceived getConfidentialDocumentType(final DocumentType documentType) {
        return NOTICE_OF_PROCEEDINGS_APP_1.equals(documentType)
            ? ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1
            : ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2;
    }
}
