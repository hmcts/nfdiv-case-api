package uk.gov.hmcts.divorce.bulkaction.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum BulkActionState {

    @CCD(
        name = "Bulk case list created"
    )
    Created("Created");

    private final String name;
}

