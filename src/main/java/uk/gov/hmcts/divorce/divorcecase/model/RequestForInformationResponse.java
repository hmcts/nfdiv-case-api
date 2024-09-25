package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

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
        label = "Date/time of request",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime requestForInformationResponseDateTime;

    @CCD(
        label = "Write your response below if the court has asked for additional information. If the court has just asked for documents, "
            + " then you do not need to write anything unless you think it's useful information.",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String requestForInformationResponseDetails;

    @CCD(
        label = "Upload documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> requestForInformationResponseDocs;

    @JsonIgnore
    public void setValues(Applicant applicant, RequestForInformationResponseParties party) {
        this.setRequestForInformationResponseParties(party);
        this.setRequestForInformationResponseName(applicant.getSolicitor().getName());
        this.setRequestForInformationResponseEmailAddress(applicant.getSolicitor().getEmail());
        this.setRequestForInformationResponseDateTime(LocalDateTime.now());
    }
}
