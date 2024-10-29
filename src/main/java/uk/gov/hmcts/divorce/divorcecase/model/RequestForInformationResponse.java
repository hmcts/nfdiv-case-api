package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.OTHER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION_RESPONSE_DOC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationResponse {

    @CCD(
        label = "Respondee",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationResponseParties",
        access = {DefaultAccess.class}
    )
    private RequestForInformationResponseParties requestForInformationResponseParties;

    @CCD(
        label = "Name",
        access = {DefaultAccess.class}
    )
    private String requestForInformationResponseName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String requestForInformationResponseEmailAddress;

    @CCD(
        label = "Date/time of response",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime requestForInformationResponseDateTime;

    @CCD(
        label = "Provided Response",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String requestForInformationResponseDetails;

    @CCD(
        label = "Uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> requestForInformationResponseDocs;

    @CCD(
        label = "Offline documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> rfiOfflineResponseDocs;

    @CCD(
        label = "Could not upload all or some requested documents",
        access = {DefaultAccess.class}
    )
    private YesOrNo requestForInformationResponseCannotUploadDocs;

    @CCD(
        label = "Offline Response",
        access = {DefaultAccess.class}
    )
    private YesOrNo requestForInformationResponseOffline;

    @CCD(
        label = "All requested documents uploaded",
        access = {DefaultAccess.class}
    )
    private YesOrNo rfiOfflineResponseAllDocumentsUploaded;

    @JsonIgnore
    public void setValues(CaseData caseData, RequestForInformationResponseParties party) {
        final Applicant applicant = party.equals(APPLICANT1) || party.equals(APPLICANT1SOLICITOR)
            ? caseData.getApplicant1()
            : caseData.getApplicant2();
        final String name = party.equals(APPLICANT1SOLICITOR) || party.equals(APPLICANT2SOLICITOR)
            ? applicant.getSolicitor().getName()
            : applicant.getFullName();
        final String email = party.equals(APPLICANT1SOLICITOR) || party.equals(APPLICANT2SOLICITOR)
            ? applicant.getSolicitor().getEmail()
            : applicant.getEmail();
        this.setRequestForInformationResponseParties(party);
        this.setRequestForInformationResponseName(name);
        this.setRequestForInformationResponseEmailAddress(email);
        this.setRequestForInformationResponseDateTime(LocalDateTime.now());

        if (party.equals(APPLICANT1)) {
            setDraftValues(caseData.getRequestForInformationList().getRequestForInformationResponseApplicant1());
        } else if (party.equals(APPLICANT2)) {
            setDraftValues(caseData.getRequestForInformationList().getRequestForInformationResponseApplicant2());
        } else if (party.equals(APPLICANT1SOLICITOR)) {
            setDraftValues(caseData.getRequestForInformationList().getRequestForInformationResponseApplicant1Solicitor());
        } else if (party.equals(APPLICANT2SOLICITOR)) {
            setDraftValues(caseData.getRequestForInformationList().getRequestForInformationResponseApplicant2Solicitor());
        }
    }

    @JsonIgnore
    public void setValues(CaseData caseData, RequestForInformationOfflineResponseDraft offlineDraft) {
        final RequestForInformationResponseParties party = getOfflineResponseParty(caseData, offlineDraft);
        String name;
        String email;
        if (party.equals(OTHER)) {
            name = offlineDraft.getRfiOfflineResponseOtherName();
            email = offlineDraft.getRfiOfflineResponseOtherEmail();
        } else {
            final Applicant applicant = party.equals(APPLICANT1) || party.equals(APPLICANT1SOLICITOR)
                ? caseData.getApplicant1()
                : caseData.getApplicant2();
            name = party.equals(APPLICANT1SOLICITOR) || party.equals(APPLICANT2SOLICITOR)
                ? applicant.getSolicitor().getName()
                : applicant.getFullName();
            email = party.equals(APPLICANT1SOLICITOR) || party.equals(APPLICANT2SOLICITOR)
                ? applicant.getSolicitor().getEmail()
                : applicant.getEmail();
        }
        this.setRequestForInformationResponseParties(party);
        this.setRequestForInformationResponseName(name);
        this.setRequestForInformationResponseEmailAddress(email);
        this.setRequestForInformationResponseDateTime(LocalDateTime.now());
        this.setRequestForInformationResponseOffline(YES);
        this.setRfiOfflineResponseAllDocumentsUploaded(offlineDraft.getRfiOfflineAllDocumentsUploaded());

        this.setRequestForInformationResponseDetails(offlineDraft.getRfiOfflineDraftResponseDetails());
        if (offlineDraft.getRfiOfflineDraftResponseDocs() != null && !offlineDraft.getRfiOfflineDraftResponseDocs().isEmpty()) {
            for (ListValue<DivorceDocument> doc : offlineDraft.getRfiOfflineDraftResponseDocs()) {
                doc.getValue().setDocumentType(REQUEST_FOR_INFORMATION_RESPONSE_DOC);
            }
            this.setRfiOfflineResponseDocs(offlineDraft.getRfiOfflineDraftResponseDocs());
        }
    }

    @JsonIgnore
    private void setDraftValues(RequestForInformationResponseDraft draft) {
        this.setRequestForInformationResponseDetails(draft.getRfiDraftResponseDetails());
        this.setRequestForInformationResponseDocs(draft.getRfiDraftResponseDocs());
        if (YES.equals(draft.getRfiDraftResponseCannotUploadDocs())) {
            this.setRequestForInformationResponseCannotUploadDocs(YES);
        }
    }

    private RequestForInformationResponseParties getOfflineResponseParty(CaseData caseData,
                                                                         RequestForInformationOfflineResponseDraft offlineDraft
    ) {
        RequestForInformationResponseParties responseParty;
        if (caseData.getApplicationType().isSole()) {
            switch (offlineDraft.getRfiOfflineSoleResponseParties()) {
                case APPLICANT -> responseParty = APPLICANT1;
                case APPLICANTSOLICITOR -> responseParty = APPLICANT1SOLICITOR;
                default -> responseParty = OTHER;
            }
        } else {
            switch (offlineDraft.getRfiOfflineJointResponseParties()) {
                case APPLICANT1 -> responseParty = APPLICANT1;
                case APPLICANT1SOLICITOR -> responseParty = APPLICANT1SOLICITOR;
                case APPLICANT2 -> responseParty = APPLICANT2;
                case APPLICANT2SOLICITOR -> responseParty = APPLICANT2SOLICITOR;
                default -> responseParty = OTHER;
            }
        }
        return responseParty;
    }

    @JsonIgnore
    public boolean isOffline() {
        return YES.equals(this.getRequestForInformationResponseOffline());
    }
}
