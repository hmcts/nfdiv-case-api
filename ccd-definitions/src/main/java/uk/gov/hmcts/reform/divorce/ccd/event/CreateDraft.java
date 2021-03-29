package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;

public class CreateDraft implements CcdConfiguration {

    public static final String CREATE_DRAFT = "create-draft";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CREATE_DRAFT)
            .initialState(Draft)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .displayOrder(1)
            .retries(120, 120);
    }
}
