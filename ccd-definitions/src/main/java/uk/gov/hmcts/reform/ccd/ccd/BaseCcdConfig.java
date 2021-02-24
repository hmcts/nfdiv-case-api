package uk.gov.hmcts.reform.ccd.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.ccd.ccd.event.PatchCase;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

public class BaseCcdConfig {

    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        new DraftCreate().buildWith(builder);
        new PatchCase().buildWith(builder);
    }
}
