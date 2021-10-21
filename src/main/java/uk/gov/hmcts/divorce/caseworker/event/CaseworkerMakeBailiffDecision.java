package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.util.AlternativeServiceUtil.archiveAlternativeServiceApplicationOnCompletion;

@Component
@Slf4j
public class CaseworkerMakeBailiffDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_BAILIFF_DECISION = "caseworker-bailiff-decision";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_BAILIFF_DECISION)
            .forState(AwaitingBailiffReferral)
            .name("Make Bailiff Decision")
            .description("Make Bailiff Decision")
            .explicitGrants()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ, LEGAL_ADVISOR)
            .grant(READ, CASE_WORKER, SOLICITOR, SYSTEMUPDATE))
            .page("makeBailiffDecision-1")
            .pageLabel("Make Bailiff Decision")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationGranted)
                .done()
            .page("makeBailiffDecision-2")
            .showCondition("serviceApplicationGranted=\"No\"")
            .pageLabel("Reason for refusal")
                .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationRefusalReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker make bailiff decision about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getAlternativeService();

        State endState;

        if (serviceApplication.getServiceApplicationGranted().toBoolean()) {
            serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));
            endState = AwaitingBailiffService;
            // ServiceApplication is archived after BailiffReturn if it is Granted
        } else {
            endState = AwaitingAos;
            archiveAlternativeServiceApplicationOnCompletion(caseDataCopy);
        }

        log.info("Setting end state of case id {} to {}", details.getId(), endState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }
}
