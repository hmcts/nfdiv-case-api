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

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationList {

    @CCD(
        label = "Requests for information",
        typeOverride = Collection,
        typeParameterOverride = "RequestForInformation",
        access = {DefaultAccess.class}
    )
    private List<ListValue<RequestForInformation>> requestsForInformation;

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
    }
}
