package uk.gov.hmcts.reform.ccd.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

public class PatchCase {

    public ConfigBuilder<CaseData, State, UserRole> buildWith(final ConfigBuilder<CaseData, State, UserRole> builder) {

        builder
            .event("patchCase")
            .forAllStates()
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .postState("*")
            .retries(120, 120)
            .fields()
            .optional(CaseData::getCreatedDate)
            .optional(CaseData::getD8caseReference)
            .optional(CaseData::getD8legalProcess);

        return builder;
    }
}
