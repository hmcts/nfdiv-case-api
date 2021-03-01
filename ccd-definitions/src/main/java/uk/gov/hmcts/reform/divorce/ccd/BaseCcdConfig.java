package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.divorce.ccd.event.PatchCase;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.divorce.ccd.model.State.DRAFT;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class BaseCcdConfig implements CcdBuilder {

    private final List<CcdBuilder> ccdBuilders = new ArrayList<>();

    public BaseCcdConfig() {
        ccdBuilders.add(new DraftCreate());
        ccdBuilders.add(new PatchCase());
    }

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.grant(DRAFT, "CRU", CITIZEN);

        configBuilder.workBasketResultFields()
            .field("createdDate", "Created date");

        configBuilder.workBasketInputFields()
            .field("createdDate", "Created date");

        for (final CcdBuilder ccdBuilder : ccdBuilders) {
            ccdBuilder.buildWith(configBuilder);
        }
    }
}
