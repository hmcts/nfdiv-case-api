package uk.gov.hmcts.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CITIZEN;

public class CreateDraft implements CcdConfiguration {

    public static final String CREATE_DRAFT = "create-draft";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CREATE_DRAFT)
            .initialState(Draft)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .displayOrder(1)
            .retries(120, 120);
    }
}
