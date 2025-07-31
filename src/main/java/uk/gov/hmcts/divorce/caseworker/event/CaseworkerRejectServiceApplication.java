package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.State.*;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerRejectServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REJECT_SERVICE_APPLICATION = "caseworker-reject-service-application";
    private static final String REJECT_SERVICE_APPLICATION = "Reject Service Application";

    private final DocumentRemovalService documentRemovalService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_REJECT_SERVICE_APPLICATION)
            .forStates(AwaitingServiceConsideration, AwaitingServicePayment, AwaitingDocuments)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name(REJECT_SERVICE_APPLICATION)
            .description(REJECT_SERVICE_APPLICATION)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("rejectServiceApplication")
            .pageLabel(REJECT_SERVICE_APPLICATION)
            .complex(CaseData::getAlternativeService)
                .label("serviceApplicationTypeLabel", "##The following service application will be rejected")
                .readonly(AlternativeService::getAlternativeServiceType)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked, Case Id: {}", CASEWORKER_REJECT_SERVICE_APPLICATION, details.getId());
        var caseData = details.getData();

        if (Objects.isNull(caseData.getAlternativeService())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of("No service application to reject."))
                .build();
        }

        if (!YesOrNo.YES.equals(caseData.getAlternativeService().getServiceApplicationSubmittedOnline())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of("Active service application cannot be rejected."))
                .build();
        }

        handleDeletionOfServiceApplicationDocuments(caseData.getAlternativeService());
        caseData.setAlternativeService(null);

        boolean isAosDrafted = caseData.getAcknowledgementOfService().getAosIsDrafted().toBoolean();
        State stateToTransition = caseData.getDueDate().isBefore(LocalDate.now()) ? AosOverdue
            : isAosDrafted ? AosDrafted
            : AwaitingDocuments;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(stateToTransition)
            .build();
    }

    private void handleDeletionOfServiceApplicationDocuments(AlternativeService alternativeService) {
        documentRemovalService.deleteDocument(alternativeService.getServiceApplicationDocuments());

        if (Objects.nonNull(alternativeService.getServiceApplicationAnswers())
            && Objects.nonNull(alternativeService.getServiceApplicationAnswers().getDocumentLink())) {
            documentRemovalService.deleteDocument(alternativeService.getServiceApplicationAnswers().getDocumentLink());
        }
    }
}
