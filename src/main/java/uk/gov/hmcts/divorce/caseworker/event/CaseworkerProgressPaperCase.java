package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.SetDefaultOrganisationPolicies;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.SUBMITTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateReadyForPayment;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerProgressPaperCase implements CCDConfig<CaseData, State, UserRole> {

    private static final String PROGRESS_PAPER_CASE = "Progress paper case";
    public static final String CASEWORKER_PROGRESS_PAPER_CASE = "caseworker-progress-paper-case";

    private final CaseFlagsService caseFlagsService;

    private final SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PROGRESS_PAPER_CASE)
            .forState(NewPaperCase)
            .name(PROGRESS_PAPER_CASE)
            .description(PROGRESS_PAPER_CASE)
            .showEventNotes()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE,
                SUPER_USER)
            .grantHistoryOnly(
                SOLICITOR,
                LEGAL_ADVISOR,
                JUDGE))
            .page(PROGRESS_PAPER_CASE)
            .pageLabel(PROGRESS_PAPER_CASE)
            .complex(CaseData::getApplication)
            .mandatory(Application::getProgressPaperCase)
            .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker progress paper case callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        log.info("Validating case data CaseID: {}", details.getId());
        final List<String> submittedErrors = validateReadyForPayment(caseData);

        if (!submittedErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(submittedErrors)
                .build();
        }

        caseFlagsService.initialiseCaseFlags(caseData);
        caseData.setCaseFlagsSetupComplete(YesOrNo.YES);

        if (caseData.getApplication().getProgressPaperCase().equals(SUBMITTED)) {
            final CaseDetails<CaseData, State> result = caseTasks(setDefaultOrganisationPolicies).run(details);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(result.getData())
                .state(Submitted)
                .build();
        } else if (caseData.getApplication().getProgressPaperCase().equals(AWAITING_DOCUMENTS)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(AwaitingDocuments)
                .build();
        } else if (caseData.getApplication().getProgressPaperCase().equals(AWAITING_HWF_DECISION)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(AwaitingHWFDecision)
                .build();
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(AwaitingPayment)
                .build();
        }
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Add payment submitted callback invoked CaseID: {}", details.getId());
        caseFlagsService.setSupplementaryDataForCaseFlags(details.getId());
        return SubmittedCallbackResponse.builder().build();
    }
}
