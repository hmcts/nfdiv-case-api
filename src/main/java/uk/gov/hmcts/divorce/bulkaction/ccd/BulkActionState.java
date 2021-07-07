package uk.gov.hmcts.divorce.bulkaction.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.access.CaseAccessAdministrator;

@RequiredArgsConstructor
@Getter
public enum BulkActionState {

    @CCD(
        name = "BulkCase scheduled for create",
        access = {CaseAccessAdministrator.class}
    )
    ScheduledForCreate("ScheduledForCreate");

    private final String name;
}

