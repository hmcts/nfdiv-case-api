package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceApplicationNotApproved;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerMakeBailiffDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_MAKE_BAILIFF_DECISION = "caseworker-make-bailiff-decision";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_MAKE_BAILIFF_DECISION)
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
            .complex(CaseData::getServiceApplication)
                .mandatory(ServiceApplication::getServiceApplicationGranted)
                .done()
            .page("makeBailiffDecision-2")
            .showCondition("serviceApplicationGranted=\"No\"")
            .pageLabel("Reason for refusal")
                .complex(CaseData::getServiceApplication)
                .mandatory(ServiceApplication::getServiceApplicationRefusalReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker make bailiff decision about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getServiceApplication();

        State endState;

        if (serviceApplication.getServiceApplicationGranted().toBoolean()) {

            serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));

            if (BAILIFF.equals(serviceApplication.getServiceApplicationType())) {
                endState = AwaitingBailiffService;
            } else {
                endState = AwaitingConditionalOrder;
            }
        } else {
            endState = ServiceApplicationNotApproved;
        }

        log.info("Setting end state of case id {} to {}", details.getId(), endState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }
}
