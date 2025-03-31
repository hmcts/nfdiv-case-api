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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
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
        label = "Offline?",
        access = {DefaultAccess.class}
    )
    private YesOrNo requestForInformationPartyOffline;

    @CCD(
        label = "Correspondence address",
        access = {DefaultAccess.class}
    )
    private String requestForInformationAddress;

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
        label = "Secondary offline?",
        access = {DefaultAccess.class}
    )
    private YesOrNo requestForInformationSecondaryPartyOffline;

    @CCD(
        label = "Secondary correspondence address",
        access = {DefaultAccess.class}
    )
    private String requestForInformationSecondaryAddress;

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

    @CCD(
        label = "Responses",
        typeOverride = Collection,
        typeParameterOverride = "RequestForInformationResponse",
        access = {DefaultAccess.class}
    )
    private List<ListValue<RequestForInformationResponse>> requestForInformationResponses;

    @JsonIgnore
    private void setNameAndEmail(Applicant applicant, Boolean setSecondary) {
        final boolean isRepresented = applicant.isRepresented();
        final String emailAddress = isRepresented ? applicant.getSolicitor().getEmail() : applicant.getEmail();
        final String name = isRepresented ? applicant.getSolicitor().getName() : applicant.getFullName();
        if (TRUE.equals(setSecondary)) {
            this.setRequestForInformationSecondaryEmailAddress(emailAddress);
            this.setRequestForInformationSecondaryName(name);
        } else {
            this.setRequestForInformationEmailAddress(emailAddress);
            this.setRequestForInformationName(name);
        }
    }

    @JsonIgnore
    public void setValues(CaseData caseData) {
        this.setRequestForInformationDateTime(LocalDateTime.now());

        final RequestForInformationSoleParties soleParties = this.getRequestForInformationSoleParties();
        final RequestForInformationJointParties jointParties = this.getRequestForInformationJointParties();
        if (RequestForInformationSoleParties.APPLICANT.equals(soleParties)
            || RequestForInformationJointParties.APPLICANT1.equals(jointParties)) {
            this.setCorrespondenceDetails(caseData.getApplicant1(), false);
        } else if (RequestForInformationJointParties.APPLICANT2.equals(jointParties)) {
            this.setCorrespondenceDetails(caseData.getApplicant2(), false);
        } else if (RequestForInformationJointParties.BOTH.equals(jointParties)) {
            this.setCorrespondenceDetails(caseData.getApplicant1(), false);
            this.setCorrespondenceDetails(caseData.getApplicant2(), true);
        }
    }

    @JsonIgnore
    public RequestForInformationAuthParty getAuthorisedResponseParty() {
        final RequestForInformationSoleParties soleParties = this.getRequestForInformationSoleParties();
        final RequestForInformationJointParties jointParties = this.getRequestForInformationJointParties();
        if (RequestForInformationSoleParties.APPLICANT.equals(soleParties)
            || RequestForInformationJointParties.APPLICANT1.equals(jointParties)) {
            return RequestForInformationAuthParty.APPLICANT1;
        } else if (RequestForInformationJointParties.APPLICANT2.equals(jointParties)) {
            return RequestForInformationAuthParty.APPLICANT2;
        } else if (RequestForInformationJointParties.BOTH.equals(jointParties)) {
            return RequestForInformationAuthParty.BOTH;
        }

        return RequestForInformationAuthParty.OTHER;
    }

    @JsonIgnore
    private void setCorrespondenceDetails(Applicant applicant, Boolean setSecondary) {
        final boolean isOffline = applicant.isApplicantOffline();
        final boolean isRepresented = applicant.isRepresented();
        final String emailAddress = isRepresented ? applicant.getSolicitor().getEmail() : applicant.getEmail();
        final String name = isRepresented ? applicant.getSolicitor().getName() : applicant.getFullName();
        final String address = applicant.getCorrespondenceAddressWithoutConfidentialCheck();
        if (TRUE.equals(setSecondary)) {
            this.setRequestForInformationSecondaryName(name);
            if (isOffline) {
                this.setRequestForInformationSecondaryPartyOffline(YesOrNo.from(isOffline));
                this.setRequestForInformationSecondaryAddress(address);
            } else {
                this.setRequestForInformationSecondaryEmailAddress(emailAddress);
            }
        } else {
            this.setRequestForInformationName(name);
            if (isOffline) {
                this.setRequestForInformationPartyOffline(YesOrNo.from(isOffline));
                this.setRequestForInformationAddress(address);
            } else {
                this.setRequestForInformationEmailAddress(emailAddress);
            }
        }
    }

    @JsonIgnore
    public void addResponseToList(RequestForInformationResponse requestForInformationResponse) {
        final ListValue<RequestForInformationResponse> newResponse = new ListValue<>();
        newResponse.setValue(requestForInformationResponse);

        if (isEmpty(this.getRequestForInformationResponses())) {
            List<ListValue<RequestForInformationResponse>> responses = new ArrayList<>();
            responses.add(newResponse);
            this.setRequestForInformationResponses(responses);
        } else {
            this.getRequestForInformationResponses().add(0, newResponse);
        }
    }

    @JsonIgnore
    public RequestForInformationResponse getLatestResponse() {
        return this.getResponseByIndex(0);
    }

    @JsonIgnore
    public RequestForInformationResponse getResponseByIndex(int index) {
        return this.getRequestForInformationResponses().get(index).getValue();
    }
}
