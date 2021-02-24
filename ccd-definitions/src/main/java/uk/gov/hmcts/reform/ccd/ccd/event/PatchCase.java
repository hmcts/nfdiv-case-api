package uk.gov.hmcts.reform.ccd.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.ccd.CcdBuilder;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.ccd.ccd.model.UserRole.CITIZEN;

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
            .grant("CRU", CITIZEN)
            .grant("CRU", CASEWORKER_DIVORCE_COURTADMIN_BETA)
            .grant("CRU", CASEWORKER_DIVORCE_COURTADMIN)
            .grant("CRU", CASEWORKER_DIVORCE_SOLICITOR)
            .grant("CRU", CASEWORKER_DIVORCE_SUPERUSER)
            .grant("CRU", CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields()
            .optional(CaseData::getCreatedDate)
            .optional(CaseData::getD8caseReference)
            .optional(CaseData::getD8legalProcess);
    }
}
