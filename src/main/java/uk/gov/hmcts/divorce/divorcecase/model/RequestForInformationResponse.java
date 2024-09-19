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
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;

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
    }
}
