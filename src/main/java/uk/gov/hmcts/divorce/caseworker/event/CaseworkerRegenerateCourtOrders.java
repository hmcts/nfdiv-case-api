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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class CaseworkerRegenerateCourtOrders implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REGENERATE_COURT_ORDERS = "caseworker-regenerate-court-orders";

    @Autowired
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Autowired
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REGENERATE_COURT_ORDERS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Regenerate court orders")
            .description("Regenerate court orders")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR))
            .page("regenerateCourtOrderDocsExplanation")
            .pageLabel("Regenerate court orders")
            .label("", "Updating court orders recreates the Certificate or Entitlement, Conditional Order and Final Order, "
                + "based on the latest case data."
                + "Any other court orders e.g. conditional order refusals, will remain unchanged.\\r\\n\\r\\n"
                + "If there have been updates to the case data e.g. change of applicant name, then these will be reflected in the updated "
                + "court orders.\\r\\n\\r\\nPrevious versions of court orders will not be stored against the case.");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker regenerate court orders callback invoked for Case Id: {}", details.getId());

        if (details.getData().getApplication().isPaperCase()) {
            log.info("Not regenerating documents(COE and CO granted) as it is paper case for Case Id: {}", details.getId());
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .build();
        }

        CaseDetails<CaseData, State> updatedDetails = null;
        if (null != details.getData().getConditionalOrder().getCertificateOfEntitlementDocument()) {
            log.info("Regenerating certificate of entitlement document for Case Id: {}", details.getId());
            updatedDetails = caseTasks(generateCertificateOfEntitlement).run(details);
        }

        if (generateConditionalOrderPronouncedDocument.getConditionalOrderGrantedDoc(details.getData()).isPresent()) {
            var caseDetails = updatedDetails == null ? details : updatedDetails;
            log.info("Regenerating CO Pronounced document for Case Id: {}", caseDetails.getId());
            updatedDetails = caseTasks(generateConditionalOrderPronouncedDocument).run(caseDetails);
        }

        if (null != updatedDetails) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(updatedDetails.getData())
                .build();
        } else {
            log.info("Certificate of entitlement and CO Pronounced doesn't exists for Case Id: {}", details.getId());
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .build();
        }
    }
}
