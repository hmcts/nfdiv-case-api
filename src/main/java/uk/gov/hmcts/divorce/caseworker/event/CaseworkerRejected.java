package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RejectReason;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.EnumSet.allOf;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CaseworkerRejected implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REJECTED = "caseworker-rejected";
    private static final String REJECT = "Reject";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_REJECTED)
            .forStateTransition(allOf(State.class), Rejected)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name(REJECT)
            .description(REJECT)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU)
            .grant(READ,
                SOLICITOR,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR))
            .page("reject")
            .pageLabel(REJECT)
            .complex(CaseData::getApplication)
                .complex(Application::getRejectReason)
                    .mandatory(RejectReason::getRejectReasonType)
                    .mandatory(RejectReason::getRejectDetails)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {
        var previousState = beforeDetails.getState();
        log.info("About to submit previousState: {} and currentState: {}", previousState, details.getState());
        var caseData = details.getData();

        caseData.getApplication().setPreviousState(previousState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
