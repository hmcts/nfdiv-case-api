package uk.gov.hmcts.reform.ccd.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

import static uk.gov.hmcts.reform.ccd.ccd.model.State.DRAFT;

public class DraftCreate {

    public ConfigBuilder<CaseData, State, UserRole> buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        builder
            .event("draftCreate")
            .initialState(DRAFT)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .displayOrder(1)
            .postState(DRAFT.getValue())
            .retries(120, 120)
            .fields()
            .optional(CaseData::getCreatedDate)
            .optional(CaseData::getD8caseReference)
            .optional(CaseData::getD8legalProcess);

        return builder;
    }
}
