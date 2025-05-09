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
        label = "Provide a statement",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedNoEvidenceStatement;

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
