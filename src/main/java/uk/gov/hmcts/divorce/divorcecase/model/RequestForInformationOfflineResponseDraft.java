package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

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
        typeParameterOverride = "DivorceDocument",
        searchable=false
    )
    private List<ListValue<DivorceDocument>> rfiOfflineDraftResponseDocs;

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

    @CCD(
        label = "All requested documents uploaded",
        access = {DefaultAccess.class}
    )
    private YesOrNo rfiOfflineAllDocumentsUploaded;

    @CCD(
        label = "Send notifications?",
        access = {DefaultAccess.class}
    )
    private YesOrNo rfiOfflineResponseSendNotifications;

    @JsonIgnore
    public void addDocument(DivorceDocument offlineResponseDoc) {
        if (this.getRfiOfflineDraftResponseDocs() == null || this.getRfiOfflineDraftResponseDocs().isEmpty()) {
            this.setRfiOfflineDraftResponseDocs(new ArrayList<>());
        }

        ListValue<DivorceDocument> offlineResponseDocListValue = new ListValue<>();
        offlineResponseDocListValue.setValue(offlineResponseDoc);
        this.getRfiOfflineDraftResponseDocs().add(0, offlineResponseDocListValue);
    }
}
