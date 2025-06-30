package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispenseWithServiceJourneyOptions {

    @CCD(
        label = "Did you and your partner live together?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseLiveTogether;

    @CCD(
        label = "Date when you last lived together",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseLivedTogetherDate;

    @CCD(
        label = "Where did you last live together?",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK dispenseLivedTogetherAddress;

    @CCD(label = "Was this an international address?")
    private YesOrNo dispenseLivedTogetherAddressOverseas;

    @CCD(
        label = "Are you aware of where your partner lived after parting?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseAwarePartnerLived;

    @CCD(
        label = "Where did your partner live after you parted?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddress1;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddressEnquiries1;

    @CCD(
        label = "Where did your partner live after you parted?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddress2;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddressEnquiries2;

    @CCD(
        label = "When was your partner last seen or heard of?",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispensePartnerLastSeenDate;

    @CCD(
        label = "Describe the time you last saw or heard of your partner",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerLastSeenDescription;
}
