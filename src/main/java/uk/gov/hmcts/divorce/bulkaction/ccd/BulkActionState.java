package uk.gov.hmcts.divorce.bulkaction.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

@RequiredArgsConstructor
@Getter
public enum BulkActionState {

    @CCD(
        label = "Bulk case list created"
    )
    Created,

    @CCD(
        label = "Bulk case listed"
    )
    Listed,

    @CCD(
        label = "Bulk case pronounced"
    )
    Pronounced,

    @CCD(
        label = "Bulk case dropped"
    )
    Dropped,

    @CCD(
        label = "Bulk case empty",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    Empty;
}

