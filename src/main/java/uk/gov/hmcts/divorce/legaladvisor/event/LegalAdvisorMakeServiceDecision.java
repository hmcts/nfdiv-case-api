package uk.gov.hmcts.divorce.legaladvisor.event;

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

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class LegalAdvisorMakeServiceDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String LEGAL_ADVISOR_SERVICE_DECISION = "legal-advisor-service-decision";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_SERVICE_DECISION)
            .forState(AwaitingServiceConsideration)
            .name("Make service decision")
            .description("Make service decision")
            .explicitGrants()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ, LEGAL_ADVISOR)
            .grant(READ, CASE_WORKER, SOLICITOR, SYSTEMUPDATE))
            .page("makeServiceDecision")
            .pageLabel("Approve service application")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationGranted)
                .mandatory(AlternativeService::getDeemedServiceDate,
                    "alternativeServiceType=\"deemed\" AND serviceApplicationGranted=\"No\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Legal advisor make service decision about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getAlternativeService();

        State endState = details.getState();

        if (serviceApplication.getServiceApplicationGranted().toBoolean()) {
            log.info("Service application granted for case id {}", details.getId());
            serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));
            endState = Holding;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }
}
