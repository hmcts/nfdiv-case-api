package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.EMAIL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_REFUSAL_COVER_LETTER;

public final class DocumentUtil {

    private static final Map<DocumentType, ConfidentialDocumentsReceived> DOCUMENTS_TYPE_TO_CONFIDENTIAL_TYPE_MAPPING
        = getDocumentTypeToConfidentialTypeMapping();

    private DocumentUtil() {
    }

    public static Document documentFrom(final DocumentInfo documentInfo) {
        return new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl());
    }

    public static DocumentInfo documentInfoFrom(final Document document) {
        return new DocumentInfo(
            document.getUrl(),
            document.getFilename(),
            document.getBinaryUrl());
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
            var letters = lettersWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), documentType);

            return letters.isEmpty() ? lettersWithDocumentType(caseData.getDocuments().getDocumentsUploaded(), documentType) : letters;
        }
    }

    private static List<Letter> lettersWithDocumentType(final List<ListValue<DivorceDocument>> documents,
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

        final boolean isAnApplicantConfidential = caseData.getApplicant1().isConfidentialContactDetails()
            || caseData.getApplicant2().isConfidentialContactDetails();

        if (EMAIL.equals(documentType)) {
            if (APPLICANT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
                return caseData.getApplicant1().isConfidentialContactDetails();
            }
            if (RESPONDENT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
                return caseData.getApplicant2().isConfidentialContactDetails();
            }
        }

        if (NOTICE_OF_PROCEEDINGS_APP_1.equals(documentType)) {
            return caseData.getApplicant1().isConfidentialContactDetails();
        }

        if (NOTICE_OF_PROCEEDINGS_APP_2.equals(documentType)) {
            return caseData.getApplicant2().isConfidentialContactDetails();
        }

        if (CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1.equals(documentType)) {
            return caseData.getApplicant1().isConfidentialContactDetails();
        }

        if (CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2.equals(documentType)) {
            return caseData.getApplicant2().isConfidentialContactDetails();
        }

        return isAnApplicantConfidential && documentType.isPotentiallyConfidential();
    }

    public static ConfidentialDocumentsReceived getConfidentialDocumentType(DocumentType documentType) {
        Map<DocumentType, ConfidentialDocumentsReceived> confidentialDocumentsMap = new HashMap<>(
            Map.of(
                NOTICE_OF_PROCEEDINGS_APP_1, ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                NOTICE_OF_PROCEEDINGS_APP_2, ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
                GENERAL_LETTER, ConfidentialDocumentsReceived.GENERAL_LETTER,
                AOS_RESPONSE_LETTER, ConfidentialDocumentsReceived.AOS_RESPONSE_LETTER,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, ConfidentialDocumentsReceived.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2, ConfidentialDocumentsReceived.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, ConfidentialDocumentsReceived.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_2, ConfidentialDocumentsReceived.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2
            )
        );

        confidentialDocumentsMap.putAll(
            Map.of(
                FINAL_ORDER_CAN_APPLY_APP1, ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP1,
                FINAL_ORDER_CAN_APPLY_APP2, ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP2,
                AOS_OVERDUE_LETTER, ConfidentialDocumentsReceived.AOS_OVERDUE_LETTER)
        );

        confidentialDocumentsMap.putAll(DOCUMENTS_TYPE_TO_CONFIDENTIAL_TYPE_MAPPING);

        return Optional.ofNullable(confidentialDocumentsMap.get(documentType)).orElse(ConfidentialDocumentsReceived.OTHER);
    }

    public static boolean removeDocumentsBasedOnContactPrivacy(final CaseData caseData, final DocumentType documentType) {
        CaseDocuments caseDocuments = caseData.getDocuments();
        if (isConfidential(caseData, documentType)) {
            return caseDocuments.removeConfidentialDocumentGeneratedWithType(getConfidentialDocumentType(documentType));
        } else {
            return caseDocuments.removeDocumentGeneratedWithType(documentType);
        }
    }

    public static boolean removeConfidentialDocuments(final CaseData caseData, final DocumentType documentType) {
        CaseDocuments caseDocuments = caseData.getDocuments();
        return caseDocuments.removeConfidentialDocumentGeneratedWithType(getConfidentialDocumentType(documentType));
    }

    public static Map<DocumentType, ConfidentialDocumentsReceived> getDocumentTypeToConfidentialTypeMapping() {
        Map<DocumentType, ConfidentialDocumentsReceived> map = new HashMap<>(
            Map.of(
                DocumentType.ACKNOWLEDGEMENT_OF_SERVICE, ConfidentialDocumentsReceived.AOS,
                DocumentType.ANNEX_A, ConfidentialDocumentsReceived.ANNEX_A,
                DocumentType.OTHER, ConfidentialDocumentsReceived.OTHER,
                DocumentType.BAILIFF_SERVICE, ConfidentialDocumentsReceived.BAILIFF_SERVICE,
                DocumentType.CONDITIONAL_ORDER_APPLICATION, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_APPLICATION,
                DocumentType.CORRESPONDENCE, ConfidentialDocumentsReceived.CORRESPONDENCE,
                DocumentType.COSTS, ConfidentialDocumentsReceived.COSTS
            )
        );

        map.putAll(
            Map.of(
                DocumentType.COSTS_ORDER, ConfidentialDocumentsReceived.COSTS_ORDER,
                DocumentType.DEEMED_SERVICE, ConfidentialDocumentsReceived.DEEMED_SERVICE,
                DocumentType.DISPENSE_WITH_SERVICE, ConfidentialDocumentsReceived.DISPENSE_WITH_SERVICE,
                DocumentType.D9D, ConfidentialDocumentsReceived.D9D,
                DocumentType.D9H, ConfidentialDocumentsReceived.D9H,
                DocumentType.EMAIL, ConfidentialDocumentsReceived.EMAIL,
                DocumentType.FINAL_ORDER_APPLICATION, ConfidentialDocumentsReceived.FINAL_ORDER_APPLICATION,
                DocumentType.WELSH_TRANSLATION, ConfidentialDocumentsReceived.WELSH_TRANSLATION,
                DocumentType.CONDITIONAL_ORDER_CAN_APPLY, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_CAN_APPLY,
                DocumentType.FINAL_ORDER_CAN_APPLY, ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY
            )
        );

        map.putAll(
            Map.of(
                DocumentType.GENERAL_LETTER, ConfidentialDocumentsReceived.GENERAL_LETTER,
                DocumentType.AOS_RESPONSE_LETTER, ConfidentialDocumentsReceived.AOS_RESPONSE_LETTER,
                DocumentType.NAME_CHANGE_EVIDENCE, ConfidentialDocumentsReceived.NAME_CHANGE,
                DocumentType.NOTICE_OF_REFUSAL_OF_ENTITLEMENT, ConfidentialDocumentsReceived.NOTICE_OF_REFUSAL_OF_ENTITLEMENT,
                DocumentType.SOLICITOR_SERVICE, ConfidentialDocumentsReceived.SOLICITOR_SERVICE,
                DocumentType.CONDITIONAL_ORDER_REMINDER, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_REMINDER,
                DocumentType.COVERSHEET, ConfidentialDocumentsReceived.COVERSHEET,
                DocumentType.SWITCH_TO_SOLE_CO_LETTER, ConfidentialDocumentsReceived.SWITCH_TO_SOLE_CO_LETTER,
                DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER,
                ConfidentialDocumentsReceived.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER,
                JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
                ConfidentialDocumentsReceived.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER
            )
        );

        map.putAll(
            Map.of(
                SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
                ConfidentialDocumentsReceived.SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
                SEPARATION_ORDER_REFUSAL_COVER_LETTER, ConfidentialDocumentsReceived.SEPARATION_ORDER_REFUSAL_COVER_LETTER,
                CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, ConfidentialDocumentsReceived.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER
            )
        );

        return map;
    }

    public static void removeExistingDocuments(CaseData caseData, List<DocumentType> documentTypesToRemove) {
        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }
    }
}
