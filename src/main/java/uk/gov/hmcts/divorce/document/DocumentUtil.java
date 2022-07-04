package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
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

    public static List<Letter> getLettersBasedOnContactPrivacy(final CaseData caseData, final DocumentType documentType) {
        if (isConfidential(caseData, documentType)) {
            return lettersWithConfidentialDocumentType(caseData.getDocuments().getConfidentialDocumentsGenerated(),
                getConfidentialDocumentType(documentType));
        } else {
            return lettersWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), documentType);
        }
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

    public static boolean isConfidential(final CaseData caseData, final boolean isApplicant1, final DivorceDocument document) {

        if (NOTICE_OF_PROCEEDINGS_APP_1.equals(document.getDocumentType())
            || GENERAL_LETTER.equals(document.getDocumentType()) && isApplicant1 && generalLettersExist(caseData, document, APPLICANT)) {
            return caseData.getApplicant1().isConfidentialContactDetails();
        } else if (NOTICE_OF_PROCEEDINGS_APP_2.equals(document.getDocumentType())
            || GENERAL_LETTER.equals(document.getDocumentType()) && !isApplicant1 && generalLettersExist(caseData, document, RESPONDENT)) {
            return caseData.getApplicant2().isConfidentialContactDetails();
        } else {
            return false;
        }
    }

    private static boolean generalLettersExist(final CaseData caseData, final DivorceDocument document, final GeneralParties party) {

        boolean existingGeneralLettersFound = ofNullable(caseData.getGeneralLetters())
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .anyMatch(generalLetterDetail -> party.equals(generalLetterDetail.getGeneralLetterParties())
                        && generalLetterDetail.getGeneralLetterLink().getUrl().equals(document.getDocumentLink().getUrl()));

        boolean newGeneralLetterFound = caseData.getGeneralLetter() != null
            && party.equals(caseData.getGeneralLetter().getGeneralLetterParties());

        return newGeneralLetterFound || existingGeneralLettersFound;
    }

    public static ConfidentialDocumentsReceived getConfidentialDocumentType(final DocumentType documentType) {
        return switch (documentType) {
            case NOTICE_OF_PROCEEDINGS_APP_1 -> ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1;
            case NOTICE_OF_PROCEEDINGS_APP_2 -> ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2;
            case GENERAL_LETTER -> ConfidentialDocumentsReceived.GENERAL_LETTER;
            default -> ConfidentialDocumentsReceived.OTHER;
        };
    }
}
