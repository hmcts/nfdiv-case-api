package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.State.DRAFT;

public class SaveAndClose implements CcdBuilder {
    public static final String SAVE_AND_CLOSE = "save-and-close";

    @Override
    public void buildWith(ConfigBuilder<CaseData, State, UserRole> builder) {
        builder
            .event(SAVE_AND_CLOSE)
            .forState(DRAFT)
            .name("Save and close")
            .description("Save and close application")
            .displayOrder(-1)
            .retries(120, 120)
            .grant("CRU", UserRole.CITIZEN)
            .aboutToSubmitWebhook(SAVE_AND_CLOSE)
            .fields()
            .optional(CaseData::getCreatedDate)
            .optional(CaseData::getD8caseReference)
            .optional(CaseData::getD8legalProcess);
    }
}
