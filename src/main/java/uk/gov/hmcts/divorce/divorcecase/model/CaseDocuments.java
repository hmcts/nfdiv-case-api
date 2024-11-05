package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerCourtAdminWithSolicitorAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.model.LetterPack;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseDocuments {

    @CCD(
        label = "Applicant 1 uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> applicant1DocumentsUploaded;

    @CCD(
        label = "Applicant 2 Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {Applicant2Access.class}
    )
    private List<ListValue<DivorceDocument>> applicant2DocumentsUploaded;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsUploaded;

    @CCD(
        label = "Confidential documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "ConfidentialDivorceDocument",
        access = {CaseworkerCourtAdminWithSolicitorAccess.class}
    )
    private List<ListValue<ConfidentialDivorceDocument>> confidentialDocumentsUploaded;

    @CCD(
        label = "Confidential documents generated",
        typeOverride = Collection,
        typeParameterOverride = "ConfidentialDivorceDocument",
        access = {CaseworkerCourtAdminWithSolicitorAccess.class}
    )
    private List<ListValue<ConfidentialDivorceDocument>> confidentialDocumentsGenerated;

    @CCD(
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsGenerated;

    @CCD(
        label = "Scanned documents",
        typeOverride = Collection,
        typeParameterOverride = "ScannedDocument",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<ScannedDocument>> scannedDocuments;

    @CCD(
        label = "Upload Answer Received supporting documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private List<ListValue<DivorceDocument>> answerReceivedSupportingDocuments;

    @CCD(
        label = "Select scanned document name",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicList scannedDocumentNames;

    @CCD(
        label = "Select general order document name",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicList generalOrderDocumentNames;

    @CCD(
        label = "Amended applications",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> amendedApplications;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsUploadedOnConfirmService;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "LetterPack",
        access = {CaseworkerCourtAdminWithSolicitorAccess.class}
    )
    private List<ListValue<LetterPack>> letterPacks;


    @CCD(
        label = "What type of document was attached?",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private OfflineDocumentReceived typeOfDocumentAttached;

    @CCD(
        label = "Scanned Form Subtype Received?",
        typeOverride = FixedList,
        typeParameterOverride = "ScannedDocumentSubtypes"
    )
    private ScannedDocumentSubtypes scannedSubtypeReceived;

    @Getter
    @AllArgsConstructor
    public enum ScannedDocumentSubtypes implements HasLabel {

        @JsonProperty("D10")
        D10("D10"),

        @JsonProperty("D84")
        D84("D84"),

        @JsonProperty("D36")
        D36("D36"),

        @JsonProperty("D10N")
        D10N("D10N"),

        @JsonProperty("D84NV")
        D84NV("D84NV"),

        @JsonProperty("D84NVA")
        D84NVA("D84NVA"),

        @JsonProperty("D36N")
        D36N("D36N");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum OfflineDocumentReceived implements HasLabel {

        @JsonProperty("D10")
        AOS_D10("Acknowledgement of service (D10)"),

        @JsonProperty("D84")
        CO_D84("Application for a conditional order (D84)"),

        @JsonProperty("D36")
        FO_D36("Application for a final order (D36)"),

        @JsonProperty("Other")
        OTHER("Other");

        private final String label;
    }

    public static <T> List<ListValue<T>> addDocumentToTop(final List<ListValue<T>> documents, final T value) {
        return addDocumentToTop(documents, value, null);
    }

    public static <T> List<ListValue<T>> addDocumentToTop(final List<ListValue<T>> documents, final T value, final String id) {
        final var listItemId = isBlank(id) ? String.valueOf(randomUUID()) : id;
        final var listValue = new ListValue<T>(listItemId, value);
        final List<ListValue<T>> list = isEmpty(documents) ? new ArrayList<>() : new ArrayList<>(documents);

        list.add(0, listValue);

        return list;
    }

    public static <T> List<ListValue<T>> sortByNewest(final List<ListValue<T>> previous, final List<ListValue<T>> updated) {
        if (isEmpty(previous)) {
            return updated;
        }

        final var previousListValueIds = previous
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        final var documentsWithoutIds =
            updated
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        return sortDocuments(documentsWithoutIds);
    }

    private static <T> List<ListValue<T>> sortDocuments(final Map<Boolean, List<ListValue<T>>> documentsWithoutIds) {
        final List<ListValue<T>> sortedDocuments = new ArrayList<>();
        final var newDocuments = documentsWithoutIds.get(true);
        final var previousDocuments = documentsWithoutIds.getOrDefault(false, new ArrayList<>());

        if (null != newDocuments) {
            sortedDocuments.addAll(0, newDocuments); // add new documents to start of the list
            sortedDocuments.addAll(1, previousDocuments);
            sortedDocuments.forEach(
                uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(randomUUID()))
            );
            return sortedDocuments;
        }

        return previousDocuments;
    }

    public static <T> boolean hasAddedDocuments(final List<ListValue<T>> after,
                                                final List<ListValue<T>> before) {

        if (isNull(before) && !after.isEmpty()) {
            return true;
        } else if (isNull(before) || isNull(after)) {
            return false;
        }

        return !after.stream()
            .allMatch(afterValue -> before.stream()
                .anyMatch(beforeValue -> Objects.equals(beforeValue.getId(), afterValue.getId())));
    }

    public static <T> boolean hasDeletedDocuments(final List<ListValue<T>> after,
                                                final List<ListValue<T>> before) {

        if (isNull(after) && !before.isEmpty()) {
            return true;
        } else if (isNull(before)) {
            return false;
        }

        return !before.stream()
            .allMatch(beforeValue -> after.stream()
                .anyMatch(afterValue -> Objects.equals(beforeValue.getId(), afterValue.getId())));
    }

    public static Optional<Document> getFirstDocumentLink(final List<ListValue<DivorceDocument>> documents,
                                                          final DocumentType documentType) {
        return Stream.ofNullable(documents)
            .flatMap(java.util.Collection::stream)
            .map(ListValue::getValue)
            .filter(divorceDocument -> documentType == divorceDocument.getDocumentType())
            .map(DivorceDocument::getDocumentLink)
            .findFirst();
    }

    @JsonIgnore
    public Optional<Document> getFirstGeneratedDocumentLinkWith(final DocumentType documentType) {
        return getFirstDocumentLink(getDocumentsGenerated(), documentType);
    }

    @JsonIgnore
    public Optional<Document> getFirstUploadedDocumentLinkWith(final DocumentType documentType) {
        return getFirstDocumentLink(getDocumentsUploaded(), documentType);
    }

    @JsonIgnore
    public boolean removeDocumentGeneratedWithType(final DocumentType documentType) {
        if (!isEmpty(this.getDocumentsGenerated())) {
            return this.getDocumentsGenerated()
                .removeIf(document -> documentType.equals(document.getValue().getDocumentType()));
        }
        return false;
    }

    @JsonIgnore
    public boolean removeConfidentialDocumentGeneratedWithType(final ConfidentialDocumentsReceived documentType) {
        if (!isEmpty(this.getConfidentialDocumentsGenerated())) {
            return this.getConfidentialDocumentsGenerated()
                .removeIf(document -> documentType.equals(document.getValue().getConfidentialDocumentsReceived()));
        }
        return false;
    }

    @JsonIgnore
    public DivorceDocument mapScannedDocumentToDivorceDocument(final ScannedDocument scannedDocument,
                                                               final DocumentType documentType,
                                                               final Clock clock) {

        return DivorceDocument.builder()
            .documentLink(scannedDocument.getUrl())
            .documentFileName(scannedDocument.getFileName())
            .documentDateAdded(LocalDate.now(clock))
            .documentType(documentType)
            .documentComment("Reclassified scanned document")
            .build();
    }

    public Optional<ListValue<DivorceDocument>> getDocumentGeneratedWithType(final DocumentType documentType) {
        return !isEmpty(this.getDocumentsGenerated())
            ? this.getDocumentsGenerated().stream()
            .filter(document -> documentType.equals(document.getValue().getDocumentType())).findFirst()
            : Optional.empty();
    }

    public boolean isGivenDocumentUnderConfidentialList(final DocumentType documentType) {
        return ofNullable(getConfidentialDocumentsGenerated())
            .orElseGet(Collections::emptyList)
            .stream().filter(Objects::nonNull)
            .anyMatch(doc -> doc.getValue() != null && getConfidentialDocumentType(documentType)
                .equals(doc.getValue().getConfidentialDocumentsReceived()));
    }
}
