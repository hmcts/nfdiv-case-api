package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessBetaOnlyAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralEmail {

    @CCD(
        label = "Address to",
        access = {CaseworkerAccessBetaOnlyAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties"
    )
    private GeneralParties generalEmailParties;

    @CCD(
        label = "Recipient's email",
        access = {CaseworkerAccessBetaOnlyAccess.class},
        typeOverride = Email
    )
    private String generalEmailOtherRecipientEmail;

    @CCD(
        label = "Recipient's name",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private String generalEmailOtherRecipientName;

    @CCD(
        label = "Please provide details",
        access = {CaseworkerAccessBetaOnlyAccess.class},
        typeOverride = TextArea
    )
    private String generalEmailDetails;

}
