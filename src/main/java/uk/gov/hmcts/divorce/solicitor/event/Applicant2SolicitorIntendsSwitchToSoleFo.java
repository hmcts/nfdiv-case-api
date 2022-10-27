package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class Applicant2SolicitorIntendsSwitchToSoleFo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO = "applicant2-intends-switch-to-sole-fo";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO)
            .forStates(AwaitingJointFinalOrder)
            .name("Intends to switch to sole FO")
            .description("Applicant 2 intends to switch to sole FO")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CITIZEN, CASE_WORKER, SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
