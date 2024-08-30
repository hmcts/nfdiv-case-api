package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformation {

    @CCD(
        label = "Address to sole",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationSoleParties",
        access = {DefaultAccess.class}
    )
    private RequestForInformationSoleParties requestForInformationSoleParties;

    @CCD(
        label = "Address to joint",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationJointParties",
        access = {DefaultAccess.class}
    )
    private RequestForInformationJointParties requestForInformationJointParties;

    @CCD(
        label = "Name",
        access = {DefaultAccess.class}
    )
    private String requestForInformationName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String requestForInformationEmailAddress;

    @CCD(
        label = "Secondary name",
        access = {DefaultAccess.class}
    )
    private String requestForInformationSecondaryName;

    @CCD(
        label = "Secondary email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String requestForInformationSecondaryEmailAddress;

    @CCD(
        label = "Date/time of request",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime requestForInformationDateTime;

    @CCD(
        label = "Please provide details",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String requestForInformationDetails;
}
