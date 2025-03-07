package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RfiResponseDocWithRfiIndex {

    @CCD(
        label = "RFI ID",
        access = {DefaultAccess.class}
    )
    private int rfiId;

    @CCD(
        label = "RFI Response ID",
        access = {DefaultAccess.class}
    )
    private int rfiResponseId;

    @CCD(
        label = "RFI Response Doc ID",
        access = {DefaultAccess.class}
    )
    private int rfiResponseDocId;

    @CCD(
        label = "RFI Response Doc",
        access = {DefaultAccess.class}
    )
    private DivorceDocument rfiResponseDoc;
}
