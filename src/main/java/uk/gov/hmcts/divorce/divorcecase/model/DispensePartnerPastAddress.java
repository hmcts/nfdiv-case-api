package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispensePartnerPastAddress {

    @CCD(
        label = "Where did your partner live after you parted?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String address;

    @CCD(
        label = "Results of any enquiries made about this address",
        access = {DefaultAccess.class}
    )
    private String addressEnquiries;
}
