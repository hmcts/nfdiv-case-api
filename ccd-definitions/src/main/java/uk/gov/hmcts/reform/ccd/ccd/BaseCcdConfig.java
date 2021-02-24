package uk.gov.hmcts.reform.ccd.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.ccd.ccd.event.PatchCase;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public class BaseCcdConfig implements CcdBuilder {

    private final List<CcdBuilder> ccdBuilders = new ArrayList<>();

    public BaseCcdConfig() {
        ccdBuilders.add(new DraftCreate());
        ccdBuilders.add(new PatchCase());
    }

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        for (final CcdBuilder ccdBuilder : ccdBuilders) {
            ccdBuilder.buildWith(builder);
        }
    }
}
