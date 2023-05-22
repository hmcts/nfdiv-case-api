package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.SetHoldingDueDate;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.SetServiceConfirmed;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAlternativeService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class CaseworkerConfirmAlternativeService implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE = "caseworker-confirm-alternative-service";

    @Autowired
    private SetHoldingDueDate setHoldingDueDate;

    @Autowired
    private SetServiceConfirmed setServiceConfirmed;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE)
            .forStates(AwaitingAlternativeService)
            .name("Confirm alternative service")
            .description("Confirm alternative service")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                SOLICITOR,
                LEGAL_ADVISOR,
                CITIZEN,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker confirm alternative service about to submit callback invoked with Case Id: {}", details.getId());

        final CaseDetails<CaseData, State> updatedDetails = caseTasks(setHoldingDueDate, setServiceConfirmed).run(details);
        final CaseData updatedData = updatedDetails.getData();
        final State endState = updatedData.isJudicialSeparationCase() ? AwaitingJsNullity : Holding;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedData)
            .state(endState)
            .build();
    }
}
