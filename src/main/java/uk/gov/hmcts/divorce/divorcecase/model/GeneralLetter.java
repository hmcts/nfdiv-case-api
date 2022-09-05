package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralLetter {

    @CCD(
        label = "Address to",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private GeneralParties generalLetterParties;

    @CCD(
        label = "Recipient's name",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private String otherRecipientName;

    @CCD(
        label = "Recipient's address",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private AddressGlobalUK otherRecipientAddress;

    @CCD(
        label = "Add attachments",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> generalLetterAttachments;

    @CCD(
        label = "Please provide details",
        typeOverride = TextArea,
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private String generalLetterDetails;

}
