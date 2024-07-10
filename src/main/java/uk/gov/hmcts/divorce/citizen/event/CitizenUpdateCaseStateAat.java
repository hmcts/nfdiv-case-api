package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenUpdateCaseStateAat implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE_CASE_STATE_AAT = "citizen-update-case-state-aat";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var updateCaseStateEnabled = Boolean.parseBoolean(System.getenv().get("CITIZEN_UPDATE_CASE_STATE_ENABLED"));
        if (updateCaseStateEnabled) {
            configBuilder
                .event(CITIZEN_UPDATE_CASE_STATE_AAT)
                .forAllStates()
                .name("Citizen update case state AAT")
                .description("Citizen update the case state in AAT")
                .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2, SYSTEMUPDATE)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE)
                .aboutToSubmitCallback(this::aboutToSubmit);
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen update case in AAT about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();
        if (EnumUtils.isValidEnum(State.class, data.getApplicant2().getMiddleName())) {
            details.setState(State.valueOf(data.getApplicant2().getMiddleName()));
            data.getApplicant2().setMiddleName("");
            log.info("Case state to be changed from {} to {}", details.getState(), data.getApplicant2().getMiddleName());
        }

        State state = details.getState();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }
}
