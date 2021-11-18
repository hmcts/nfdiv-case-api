package uk.gov.hmcts.divorce.bulkaction.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

@RequiredArgsConstructor
@Getter
public enum BulkActionState {

    @CCD(
        name = "Bulk case list created"
    )
    Created("Created"),

    @CCD(
        name = "Bulk case listed"
    )
    Listed("Listed"),

    @CCD(
        name = "Bulk case pronounced"
    )
    Pronounced("Pronounced"),

    @CCD(
        name = "Bulk case dropped"
    )
    Dropped("Dropped"),

    @CCD(
        name = "Bulk case empty",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    Empty("Empty");

    private final String name;
}

