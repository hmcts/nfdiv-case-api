package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationList {

    @CCD(
        label = "Requests for information",
        typeOverride = Collection,
        typeParameterOverride = "RequestForInformation",
        access = {DefaultAccess.class}
    )
    private List<ListValue<RequestForInformation>> requestsForInformation;

    @CCD(
        label = "The court has made the following comments:",
        access = {DefaultAccess.class}
    )
    private String latestRequestForInformationDetails;

    @CCD(
        label = "Request for information",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    @JsonUnwrapped
    private RequestForInformation requestForInformation = new RequestForInformation();

    @CCD(
        label = "Request for information response Applicant 1 Draft",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    @JsonUnwrapped(prefix = "app1")
    private RequestForInformationResponseDraft requestForInformationResponseApplicant1 = new RequestForInformationResponseDraft();

    @CCD(
        label = "Request for information response Applicant 1 Solicitor Draft",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    @JsonUnwrapped(prefix = "app1Sol")
    private RequestForInformationResponseDraft requestForInformationResponseApplicant1Solicitor = new RequestForInformationResponseDraft();

    @CCD(
        label = "Request for information response Applicant 2 Draft",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    @Builder.Default
    @JsonUnwrapped(prefix = "app2")
    private RequestForInformationResponseDraft requestForInformationResponseApplicant2 = new RequestForInformationResponseDraft();

    @CCD(
        label = "Request for information response Applicant 2 Solicitor Draft",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    @JsonUnwrapped(prefix = "app2Sol")
    private RequestForInformationResponseDraft requestForInformationResponseApplicant2Solicitor = new RequestForInformationResponseDraft();

    @CCD(
        label = "Request for information Offline response Draft",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    @Builder.Default
    @JsonUnwrapped()
    private RequestForInformationOfflineResponseDraft requestForInformationOfflineResponseDraft
        = new RequestForInformationOfflineResponseDraft();

    @CCD(
        label = "Authorised Request For Information Response Party",
        access = {DefaultAccess.class}
    )
    private RequestForInformationAuthParty requestForInformationAuthParty;

    @CCD(
        label = "Temp Response Document Collection with Indexes",
        typeOverride = Collection,
        typeParameterOverride = "RfiResponseDocWithRfiIndex",
        access = {DefaultAccess.class}
    )
    private List<ListValue<RfiResponseDocWithRfiIndex>> responseDocsWithIndexes;

    @CCD(
        label = "Temp Offline Response Document Collection with Indexes",
        typeOverride = Collection,
        typeParameterOverride = "RfiResponseDocWithRfiIndex",
        access = {DefaultAccess.class}
    )
    private List<ListValue<RfiResponseDocWithRfiIndex>> offlineResponseDocsWithIndexes;

    @CCD(
        label = "Request For Information Online Response Documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> rfiOnlineResponseDocuments;

    @JsonIgnore
    private boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    @JsonIgnore
    private void addResponseDocToCollection(
        int rfiId,
        int rfiResponseId,
        int rfiDocId,
        DivorceDocument rfiResponseDoc,
        List<ListValue<RfiResponseDocWithRfiIndex>> targetCollection
    ) {
        RfiResponseDocWithRfiIndex doc = new RfiResponseDocWithRfiIndex();
        doc.setRfiId(rfiId);
        doc.setRfiResponseId(rfiResponseId);
        doc.setRfiResponseDocId(rfiDocId);
        doc.setRfiResponseDoc(rfiResponseDoc);

        ListValue<DivorceDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(rfiResponseDoc);

        ListValue<RfiResponseDocWithRfiIndex> docListValue = new ListValue<>();
        docListValue.setValue(doc);

        targetCollection.add(docListValue);
    }

    @JsonIgnore
    public RequestForInformation getLatestRequest() {
        return this.getRequestsForInformation().get(0).getValue();
    }

    @JsonIgnore
    public void addRequestToList(RequestForInformation requestForInformation) {
        final ListValue<RequestForInformation> newRequest = new ListValue<>();
        newRequest.setValue(requestForInformation);

        if (isEmpty(this.getRequestsForInformation())) {
            List<ListValue<RequestForInformation>> requests = new ArrayList<>();
            requests.add(newRequest);
            this.setRequestsForInformation(requests);
        } else {
            this.getRequestsForInformation().add(0, newRequest);
        }

        this.setRequestForInformationAuthParty(requestForInformation.getAuthorisedResponseParty());
        this.setLatestRequestForInformationDetails(requestForInformation.getRequestForInformationDetails());
    }

    @JsonIgnore
    public void buildResponseDocList() {
        buildTempDocLists();
        this.setRfiOnlineResponseDocuments(null);
        if (!isNullOrEmpty(this.getResponseDocsWithIndexes())) {
            List<ListValue<DivorceDocument>> responseDocs = new ArrayList<>();
            this.getResponseDocsWithIndexes().forEach(doc -> {
                ListValue<DivorceDocument> responseDoc = new ListValue<>();
                responseDoc.setValue(doc.getValue().getRfiResponseDoc());
                responseDocs.add(responseDoc);
            });
            this.setRfiOnlineResponseDocuments(responseDocs);
        }
    }

    @JsonIgnore
    public void buildTempDocLists() {
        if (!isNullOrEmpty(this.getRequestsForInformation())) {
            clearTempDocLists();
            for (int rfiIdx = 0; rfiIdx < this.getRequestsForInformation().size(); rfiIdx += 1) {
                final RequestForInformation rfi = this.getRequestsForInformation().get(rfiIdx).getValue();
                if (!isNullOrEmpty(rfi.getRequestForInformationResponses())) {
                    for (int resIdx = 0; resIdx < rfi.getRequestForInformationResponses().size(); resIdx += 1) {
                        final RequestForInformationResponse rfiResponse = rfi.getRequestForInformationResponses().get(resIdx).getValue();
                        if (!isNullOrEmpty(rfiResponse.getRequestForInformationResponseDocs())) {
                            if (isNullOrEmpty(this.getResponseDocsWithIndexes())) {
                                this.setResponseDocsWithIndexes(new ArrayList<>());
                            }
                            for (int docIdx = 0; docIdx < rfiResponse.getRequestForInformationResponseDocs().size(); docIdx += 1) {
                                final DivorceDocument rfiResponseDoc =
                                    rfiResponse.getRequestForInformationResponseDocs().get(docIdx).getValue();
                                addResponseDocToCollection(rfiIdx, resIdx, docIdx, rfiResponseDoc, this.getResponseDocsWithIndexes());
                            }
                        }

                        if (!isNullOrEmpty(rfiResponse.getRfiOfflineResponseDocs())) {
                            if (isNullOrEmpty(this.getOfflineResponseDocsWithIndexes())) {
                                this.setOfflineResponseDocsWithIndexes(new ArrayList<>());
                            }
                            for (int docIdx = 0; docIdx < rfiResponse.getRfiOfflineResponseDocs().size(); docIdx += 1) {
                                final DivorceDocument rfiOfflineResponseDoc =
                                    rfiResponse.getRfiOfflineResponseDocs().get(docIdx).getValue();
                                addResponseDocToCollection(
                                    rfiIdx,
                                    resIdx,
                                    docIdx,
                                    rfiOfflineResponseDoc,
                                    this.getOfflineResponseDocsWithIndexes()
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    @JsonIgnore
    public void clearTempDocLists() {
        this.setResponseDocsWithIndexes(null);
        this.setOfflineResponseDocsWithIndexes(null);
    }

    @JsonIgnore
    public RequestForInformation getRequestForInformationByIndex(int index) {
        return this.getRequestsForInformation().get(index).getValue();
    }
}
