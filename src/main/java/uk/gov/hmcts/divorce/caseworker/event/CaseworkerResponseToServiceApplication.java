package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerResponseToServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION = "caseworker-response-to-service-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION)
            .forStates(AwaitingAos, AosOverdue)
            .name("Response to service app")
            .description("Response to service application")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR))
            .page("uploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getDocuments)
            .optional(CaseDocuments::getDocumentsUploaded)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker response to service application about to submit callback invoked");

        CaseData caseData = details.getData();
        AlternativeServiceType alternativeServiceType = caseData.getAlternativeService().getAlternativeServiceType();

        State state = details.getState();

        if (DEEMED.equals(alternativeServiceType) || DISPENSED.equals(alternativeServiceType)) {
            state = AwaitingServiceConsideration;
        } else if (BAILIFF.equals(alternativeServiceType)) {
            state = AwaitingBailiffReferral;
        } else {
            log.error("alternate service type '{}' doesn't match any of the required options, "
                + "defaulting to current state", alternativeServiceType);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
