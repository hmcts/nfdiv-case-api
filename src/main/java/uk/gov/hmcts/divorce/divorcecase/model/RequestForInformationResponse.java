package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        label = "Please provide details",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String requestForInformationResponseDetails;

    @CCD(
        label = "Documents uploaded for the Request for Information Response",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> requestForInformationResponseDocs;
}
