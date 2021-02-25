package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent.PATCH_CASE;

public class PatchCase implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PATCH_CASE.name)
            .forAllStates()
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .postState("*")
            .retries(120, 120)
            .grant("CRU", UserRole.CITIZEN)
            .grant("R", UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA)
            .grant("R", UserRole.CASEWORKER_DIVORCE_COURTADMIN)
            .grant("R", UserRole.CASEWORKER_DIVORCE_SOLICITOR)
            .grant("R", UserRole.CASEWORKER_DIVORCE_SUPERUSER)
            .grant("R", UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA);
    }
}
