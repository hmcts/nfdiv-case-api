package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CitizenUpdateCaseStateAat implements CCDConfig<CaseData, State, UserRole> {

    private static final String ENVIRONMENT_AAT = "aat";

    public static final String CITIZEN_UPDATE_CASE_STATE_AAT = "citizen-update-case-state-aat";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE_CASE_STATE_AAT)
            .forAllStates()
            .name("Citizen update case state AAT")
            .description("Citizen update the case state in AAT")
            .grant(CREATE_READ_UPDATE, CITIZEN, APPLICANT_2)
            .grant(READ, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        if (isEnvironmentAat()) {
            log.info("Citizen update case state in AAT about to submit callback invoked");
            State state = State.valueOf(data.getApplicant2().getSolicitor().getAddress());
            data.getApplicant2().getSolicitor().setAddress(null);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(state)
                .build();
        }

        log.info("Error updating case state as environment is {}",
            System.getenv().getOrDefault("ENVIRONMENT", null));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();

    }

    public boolean isEnvironmentAat() {
        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);
        return null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT);
    }
}

