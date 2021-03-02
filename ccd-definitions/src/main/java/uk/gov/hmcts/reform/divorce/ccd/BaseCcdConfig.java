package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.divorce.ccd.event.PatchCase;
import uk.gov.hmcts.reform.divorce.ccd.event.SaveAndClose;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public class BaseCcdConfig implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        for (final CaseEvent event : CaseEvent.values()) {
            event.builder.buildWith(builder);
        }

    }
}
