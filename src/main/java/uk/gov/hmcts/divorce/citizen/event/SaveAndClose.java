package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CITIZEN;

@Component
public class SaveAndClose implements CCDConfig<CaseData, State, UserRole> {

    public static final String SAVE_AND_CLOSE = "save-and-close";

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SAVE_AND_CLOSE)
            .forState(Draft)
            .name("Save and close application")
            .description("Save application and send email notification to petitioner")
            .displayOrder(1)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .submittedCallback(this::submitted);
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        saveAndSignOutNotificationHandler.notifyApplicant(details.getData());

        return SubmittedCallbackResponse.builder().build();
    }
}
