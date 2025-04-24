package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeemedServiceJourneyOptions {

    @CCD(
            label = "You're about to apply for deemed service",
            access = {DefaultAccess.class}
    )
    private YesOrNo deemedIUnderstand;

    @CCD(
        label = "Will you be using Help with Fees for this application?",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedUseHelpWithFees;

    @CCD(
        label = "Do you have a help with fees reference number?",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedHaveHwfReference;

    @CCD(
        label = "Are you able to upload evidence?",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedCanUploadEvidence;

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = {DefaultAccess.class}
    )
    private String deemedHwfRefNumber;

    @CCD(
        label = "Provide a statement",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedNoEvidenceStatement;

    @CCD(
        label = "Upload documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> deemedEvidenceDocs;

    @CCD(
        label = "Cannot upload some or all of my documents",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedCannotUploadDocs;

    @CCD(
        label = "Tell us about your evidence",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedEvidenceDetails;

    @CCD(
        label = "Statement of Truth",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedStatementOfTruth;
}
