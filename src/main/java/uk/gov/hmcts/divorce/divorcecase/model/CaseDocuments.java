package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerCourtAdminWithSolicitorAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

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
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsGenerated;

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

    public void sortApplicant1UploadedDocuments(List<ListValue<DivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds =
            this.getApplicant1DocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        this.setApplicant1DocumentsUploaded(sortDocuments(documentsWithoutIds));
    }

    public void sortUploadedDocuments(List<ListValue<DivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds =
            this.getDocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        this.setDocumentsUploaded(sortDocuments(documentsWithoutIds));
    }

    public void sortConfidentialDocuments(List<ListValue<ConfidentialDivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<ConfidentialDivorceDocument>>> documentsWithoutIds =
            this.getConfidentialDocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        List<ListValue<ConfidentialDivorceDocument>> sortedDocuments = new ArrayList<>();
        sortedDocuments.addAll(0, documentsWithoutIds.get(true)); // add new documents to start of the list
        sortedDocuments.addAll(1, documentsWithoutIds.get(false));

        sortedDocuments.forEach(
            uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(UUID.randomUUID()))
        );

        this.setConfidentialDocumentsUploaded(sortedDocuments);
    }

    private List<ListValue<DivorceDocument>> sortDocuments(final Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds) {

        final List<ListValue<DivorceDocument>> sortedDocuments = new ArrayList<>();

        final var newDocuments = documentsWithoutIds.get(true);
        final var previousDocuments = documentsWithoutIds.get(false);

        if (null != newDocuments) {
            sortedDocuments.addAll(0, newDocuments); // add new documents to start of the list
            sortedDocuments.addAll(1, previousDocuments);
            sortedDocuments.forEach(
                uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(UUID.randomUUID()))
            );
            return sortedDocuments;
        }

        return previousDocuments;
    }

    public void addToDocumentsGenerated(final ListValue<DivorceDocument> listValue) {

        final List<ListValue<DivorceDocument>> documents = getDocumentsGenerated();

        if (isEmpty(documents)) {
            final List<ListValue<DivorceDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsGenerated(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
    }

    public void addToDocumentsUploaded(final ListValue<DivorceDocument> listValue) {

        final List<ListValue<DivorceDocument>> documents = getDocumentsUploaded();

        if (isEmpty(documents)) {
            final List<ListValue<DivorceDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsUploaded(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
    }

}
