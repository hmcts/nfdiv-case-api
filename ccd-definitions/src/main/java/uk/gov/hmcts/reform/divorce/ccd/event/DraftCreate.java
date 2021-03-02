package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.State.DRAFT;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.*;

public class DraftCreate implements CcdBuilder {

    public static final String DRAFT_CREATE = "draftCreate";

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        builder
            .event(DRAFT_CREATE)
            .initialState(DRAFT)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .displayOrder(1)
            .postState(DRAFT.getValue())
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
