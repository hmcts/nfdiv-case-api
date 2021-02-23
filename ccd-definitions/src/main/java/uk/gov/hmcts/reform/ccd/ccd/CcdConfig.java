package uk.gov.hmcts.reform.ccd.ccd;

import uk.gov.hmcts.ccd.sdk.types.BaseCCDConfig;
import uk.gov.hmcts.ccd.sdk.types.Webhook;
import uk.gov.hmcts.reform.ccd.ccd.model.CaseData;
import uk.gov.hmcts.reform.ccd.ccd.model.State;
import uk.gov.hmcts.reform.ccd.ccd.model.UserRole;

import static uk.gov.hmcts.reform.ccd.ccd.model.State.DRAFT;

public class CcdConfig extends BaseCCDConfig<CaseData, State, UserRole> {

    @Override
    protected void configure() {

        caseType("NO_FAULT_DIVORCE");
        setEnvironment(environment());
        setWebhookConvention(this::webhookConvention);

        event("draftCreate")
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

        event("patchCase")
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

    }

    protected String environment() {
        return "development";
    }

    protected String webhookConvention(final Webhook webhook, final String eventId) {
        return "localhost:5050/" + eventId + "/" + webhook;
    }
}
