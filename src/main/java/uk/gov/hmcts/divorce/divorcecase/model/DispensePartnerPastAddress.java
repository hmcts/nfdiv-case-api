package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispensePartnerPastAddress {

    @CCD(
        label = "Where did your partner live after you parted?",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK address;

    @CCD(label = "Was this an international address?")
    private YesOrNo addressOverseas;

    @CCD(
        label = "Results of any enquiries made about this address",
        access = {DefaultAccess.class}
    )
    private String addressEnquiries;
}
