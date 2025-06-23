package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Address;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobal;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CitizenAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccessExcludingSolicitor;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

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
        typeOverride = Collection,
        typeParameterOverride = "AddressGlobalUK",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<AddressGlobalUK>> dispensePartnerAddressList;
}
