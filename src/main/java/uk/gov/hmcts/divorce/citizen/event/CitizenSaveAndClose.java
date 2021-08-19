package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenSaveAndClose implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SAVE_AND_CLOSE = "citizen-save-and-close";

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SAVE_AND_CLOSE)
            .forAllStates()
            .name("Save and close application")
            .description("Save application and send email notification to the applicant")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .submittedCallback(this::submitted);
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        saveAndSignOutNotificationHandler.notifyApplicant(details.getData(), user.getUserDetails());

        return SubmittedCallbackResponse.builder().build();
    }
}
