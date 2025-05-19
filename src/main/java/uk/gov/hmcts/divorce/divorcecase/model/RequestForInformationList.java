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
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationList {

    public static final String RFI_DOCUMENT_REMOVED_NOTICE = "** Document Removed **";

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
        label = "Request For Information Online Response Documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> rfiOnlineResponseDocuments;

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
        this.setRfiOnlineResponseDocuments(null);
        if (this.getRequestsForInformation() != null && !this.getRequestsForInformation().isEmpty()) {
            List<ListValue<DivorceDocument>> responseDocs = new ArrayList<>();
            this.getRequestsForInformation().forEach(rfi -> {
                if (rfi.getValue().getRequestForInformationResponses() != null
                    && !rfi.getValue().getRequestForInformationResponses().isEmpty()
                ) {
                    rfi.getValue().getRequestForInformationResponses().forEach(rfiResponseValue -> {
                        RequestForInformationResponse response = rfiResponseValue.getValue();

                        if (response != null && !response.isOffline() && response.getRequestForInformationResponseDocs() != null) {
                            responseDocs.addAll(response.getRequestForInformationResponseDocs());
                        }
                    });
                }
            });
            if (!responseDocs.isEmpty()) {
                this.setRfiOnlineResponseDocuments(responseDocs);
            }
        }
    }

    @JsonIgnore
    public void clearResponseDocList() {
        this.setRfiOnlineResponseDocuments(null);
    }

    @JsonIgnore
    public void deleteRfiResponseDocuments(List<ListValue<DivorceDocument>> documentsToRemove) {
        if (this.getRequestsForInformation() != null && !this.getRequestsForInformation().isEmpty()) {
            this.getRequestsForInformation().forEach(rfi -> {
                if (rfi.getValue().getRequestForInformationResponses() != null
                    && !rfi.getValue().getRequestForInformationResponses().isEmpty()
                ) {
                    rfi.getValue().getRequestForInformationResponses().forEach(rfiResponse -> {
                        List<ListValue<DivorceDocument>> rfiDocsToRemove = new ArrayList<>();
                        List<ListValue<DivorceDocument>> responseDocs;
                        if (rfiResponse.getValue().isOffline()) {
                            responseDocs = rfiResponse.getValue().getRfiOfflineResponseDocs();
                        } else {
                            responseDocs = rfiResponse.getValue().getRequestForInformationResponseDocs();
                        }

                        if (responseDocs != null && !responseDocs.isEmpty()) {
                            responseDocs.forEach(responseDoc -> {
                                Optional<ListValue<DivorceDocument>> responseDocOptional =
                                    emptyIfNull(documentsToRemove)
                                        .stream()
                                        .filter(docToRemove -> docToRemove.getValue().equals(responseDoc.getValue()))
                                        .findFirst();

                                if (responseDocOptional.isPresent()) {
                                    String responseDetails = rfiResponse.getValue().getRequestForInformationResponseDetails();
                                    responseDetails = isNullOrEmpty(responseDetails)
                                        ? RFI_DOCUMENT_REMOVED_NOTICE
                                        : RFI_DOCUMENT_REMOVED_NOTICE + "\n\n" + responseDetails;
                                    rfiResponse.getValue().setRequestForInformationResponseDetails(responseDetails);

                                    rfiDocsToRemove.add(responseDoc);
                                }
                            });

                            if (!rfiDocsToRemove.isEmpty()) {
                                rfiDocsToRemove.forEach(responseDocs::remove);
                            }
                        }
                    });
                }
            });
        }
    }
}
