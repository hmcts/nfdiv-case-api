package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenAddPartnerContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_ADD_PARTNER_DETAILS = "citizen-add-partner-details";

    private static final EnumSet<State> CITIZEN_UPDATE_STATES = EnumSet.complementOf(EnumSet.of(
        AwaitingPayment,
        NewPaperCase,
        Submitted,
        Withdrawn,
        Rejected,
        Archived
    ));

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_ADD_PARTNER_DETAILS)
            .forStates(CITIZEN_UPDATE_STATES)
            .showCondition(NEVER_SHOW)
            .name("Add partner contact details")
            .description("Add partner contact details")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        State currentState = details.getState();

        if (State.AwaitingDocuments.equals(currentState) && !caseData.getApplication().getApplicant1CannotUpload().toBoolean()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(caseData)
                    .state(Submitted)
                    .build();
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(currentState)
                .build();
    }
}
