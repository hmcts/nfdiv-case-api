package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CitizenSaveAndClose implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SAVE_AND_CLOSE = "citizen-save-and-close";

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SAVE_AND_CLOSE)
            .forAllStates()
            .showCondition(NEVER_SHOW)
            .name("Save and close application")
            .description("Save application and send email notification to the applicant")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN, APPLICANT_2)
            .submittedCallback(this::submitted);
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("CitizenSaveAndClose submitted callback invoked for case id: {}", details.getId());

        saveAndSignOutNotificationHandler.notifyApplicant(details.getData(), details.getId(), request.getHeader(AUTHORIZATION));

        return SubmittedCallbackResponse.builder().build();
    }
}
