package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationOfflineResponseDraft {

    @CCD(
        label = "Details",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String rfiOfflineDraftResponseDetails;

    @CCD(
        label = "Documents",
        typeOverride = Collection,
        typeParameterOverride = "RequestForInformationOfflineResponseDoc"
    )
    private List<ListValue<RequestForInformationOfflineResponseDoc>> rfiOfflineDraftResponseDocs;

    @CCD(
        label = "Select sender of document",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationOfflineResponseSoleParties",
        access = {DefaultAccess.class}
    )
    private RequestForInformationOfflineResponseSoleParties rfiOfflineSoleResponseParties;

    @CCD(
        label = "Select sender of document",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationOfflineResponseJointParties",
        access = {DefaultAccess.class}
    )
    private RequestForInformationOfflineResponseJointParties rfiOfflineJointResponseParties;

    @CCD(
        label = "Name",
        access = {DefaultAccess.class}
    )
    private String rfiOfflineResponseOtherName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String rfiOfflineResponseOtherEmail;

    @JsonIgnore
    public RequestForInformationOfflineResponseDoc getLatestDocument() {
        return this.getRfiOfflineDraftResponseDocs().get(0).getValue();
    }

    @JsonIgnore
    public void addDocument(RequestForInformationOfflineResponseDoc offlineResponseDoc) {
        if (this.getRfiOfflineDraftResponseDocs() == null || this.getRfiOfflineDraftResponseDocs().isEmpty()) {
            this.setRfiOfflineDraftResponseDocs(new ArrayList<>());
        }

        ListValue<RequestForInformationOfflineResponseDoc> offlineResponseDocListValue = new ListValue<>();
        offlineResponseDocListValue.setValue(offlineResponseDoc);
        this.getRfiOfflineDraftResponseDocs().add(0, offlineResponseDocListValue);
    }
}
