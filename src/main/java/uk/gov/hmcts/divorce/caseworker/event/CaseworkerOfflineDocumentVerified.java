package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerOfflineDocumentVerified implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED = "caseworker-offline-document-verified";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)
            .forState(OfflineDocumentReceived)
            .name("Offline Document Verified")
            .description("Offline Document Verified")
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER))
            .page("Update case state")
            .pageLabel("Update case state")
            .complex(CaseData::getApplication)
            .mandatory(Application::getStateToTransitionApplicationTo)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData data = details.getData();
        final State state = data.getApplication().getStateToTransitionApplicationTo();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }
}
