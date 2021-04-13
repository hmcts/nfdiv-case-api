package uk.gov.hmcts.divorce.api.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.READ;
import static uk.gov.hmcts.divorce.api.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CITIZEN;

@Component
public class PatchCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String PATCH_CASE = "patch-case";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PATCH_CASE)
            .forState(Draft)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ, CASEWORKER_DIVORCE_SUPERUSER);
    }
}
