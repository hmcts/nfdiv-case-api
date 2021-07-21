package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

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
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties"
    )
    private GeneralParties generalEmailParties;

    @CCD(
        label = "Recipient's email",
        typeOverride = Email
    )
    private String generalEmailOtherRecipientEmail;

    @CCD(
        label = "Recipient's name"
    )
    private String generalEmailOtherRecipientName;

    @CCD(
        label = "Please provide details",
        typeOverride = TextArea
    )
    private String generalEmailDetails;

}
