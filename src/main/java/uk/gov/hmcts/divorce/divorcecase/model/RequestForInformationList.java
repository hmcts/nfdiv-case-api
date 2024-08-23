package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationList {

    @CCD(
        label = "Requests for information",
        typeOverride = Collection,
        typeParameterOverride = "RequestForInformation",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<RequestForInformation>> requestsForInformation;

    @CCD(
        label = "Request for information"
    )
    @Builder.Default
    @JsonUnwrapped
    private RequestForInformation requestForInformation = new RequestForInformation();
}
