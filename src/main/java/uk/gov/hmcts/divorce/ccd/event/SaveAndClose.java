package uk.gov.hmcts.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CITIZEN;

public class SaveAndClose implements CcdConfiguration {

    public static final String SAVE_AND_CLOSE = "save-and-close";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SAVE_AND_CLOSE)
            .forState(Draft)
            .name("Save and close application")
            .description("Save application and send email notification to applicant")
            .displayOrder(1)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .submittedWebhook(SAVE_AND_CLOSE);
    }
}
