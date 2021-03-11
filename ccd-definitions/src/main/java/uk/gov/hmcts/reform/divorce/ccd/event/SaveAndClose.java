package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;

public class SaveAndClose implements CcdConfiguration {

    public static final String SAVE_AND_CLOSE = "save-and-close";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SAVE_AND_CLOSE)
            .initialState(Draft)
            .name("Save and close application")
            .description("Save application and send email notification to petitioner")
            .displayOrder(1)
            .retries(120, 120)
            .aboutToSubmitWebhook(SAVE_AND_CLOSE);
    }
}
