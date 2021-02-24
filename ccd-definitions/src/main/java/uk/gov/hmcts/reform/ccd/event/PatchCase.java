package uk.gov.hmcts.reform.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.CcdBuilder;
import uk.gov.hmcts.reform.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.State;
import uk.gov.hmcts.reform.ccd.model.UserRole;

public class PatchCase implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        builder
            .event("patchCase")
            .forAllStates()
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .postState("*")
            .retries(120, 120)
            .grant("CRU", UserRole.CITIZEN)
            .grant("CRU", UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA)
            .grant("CRU", UserRole.CASEWORKER_DIVORCE_COURTADMIN)
            .grant("CRU", UserRole.CASEWORKER_DIVORCE_SOLICITOR)
            .grant("CRU", UserRole.CASEWORKER_DIVORCE_SUPERUSER)
            .grant("CRU", UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields()
            .optional(CaseData::getCreatedDate)
            .optional(CaseData::getD8caseReference)
            .optional(CaseData::getD8legalProcess);
    }
}
