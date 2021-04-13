package uk.gov.hmcts.divorce.api.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.api.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CITIZEN;

@Component
public class SaveAndClose implements CCDConfig<CaseData, State, UserRole> {

    public static final String SAVE_AND_CLOSE = "save-and-close";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SAVE_AND_CLOSE)
            .forState(Draft)
            .name("Save and close application")
            .description("Save application and send email notification to petitioner")
            .displayOrder(1)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN);
        //  .submittedWebhook(SAVE_AND_CLOSE);
    }
}
