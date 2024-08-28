package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

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
        typeParameterOverride = "RequestForInformationSoleParties"
    )
    private RequestForInformationSoleParties requestForInformationSoleParties;

    @CCD(
        label = "Address to joint",
        typeOverride = FixedList,
        typeParameterOverride = "RequestForInformationJointParties"
    )
    private RequestForInformationJointParties requestForInformationJointParties;

    @CCD(
        label = "Name"
    )
    private String requestForInformationName;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String requestForInformationEmailAddress;

    @CCD(
        label = "Secondary Name"
    )
    private String requestForInformationSecondaryName;

    @CCD(
        label = "Secondary Email address",
        typeOverride = Email
    )
    private String requestForInformationSecondaryEmailAddress;

    @CCD(
        label = "Date/Time Of Request"
    )
    private LocalDateTime requestForInformationDateTime;

    @CCD(
        label = "Please provide details",
        typeOverride = TextArea
    )
    private String requestForInformationDetails;
}
