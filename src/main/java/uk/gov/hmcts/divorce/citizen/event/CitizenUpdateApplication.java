package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.ManageCaseTtl.MANAGE_CASE_TTL;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitizenUpdateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE = "citizen-update-application";

    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE)
            .forStates(Draft, AwaitingApplicant1Response, ConditionalOrderDrafted, ConditionalOrderPending,
                AwaitingClarification, AwaitingFinalOrder, AwaitingFinalOrderPayment, AwaitingJointFinalOrder,
                AwaitingRequestedInformation, InformationRequested, RequestedInformationSubmitted)
            .showCondition(NEVER_SHOW)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        //Set TTL for newly created cases from today's date to 6 months in future
        if (caseData.getRetainAndDisposeTimeToLive() == null && details.getState() == Draft) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();

            ccdUpdateService.submitEvent(details.getId(), MANAGE_CASE_TTL, user, serviceAuthorization);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .build();
    }
}
