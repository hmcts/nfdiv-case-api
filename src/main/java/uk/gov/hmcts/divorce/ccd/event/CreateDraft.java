package uk.gov.hmcts.divorce.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CITIZEN;

@Component
public class CreateDraft implements CCDConfig<CaseData, State, UserRole> {

    public static final String CREATE_DRAFT = "create-draft";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

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
