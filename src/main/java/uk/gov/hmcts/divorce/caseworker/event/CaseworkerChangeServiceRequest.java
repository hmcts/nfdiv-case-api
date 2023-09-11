package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateChangeServiceRequest;

@Component
@Slf4j
public class CaseworkerChangeServiceRequest implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CHANGE_SERVICE_REQUEST = "caseworker-change-service-request";

    @Autowired
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Autowired
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Autowired
    private GenerateD10Form generateD10Form;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CHANGE_SERVICE_REQUEST)
            .forStates(Submitted, AwaitingDocuments, AwaitingHWFDecision)
            .name("Change service request")
            .description("Change service request")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(SOLICITOR, LEGAL_ADVISOR, JUDGE))
            .page("changeServiceRequest")
            .pageLabel("Change service request")
            .complex(CaseData::getApplication)
            .mandatory(Application::getServiceMethod);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();
        final Application application = caseData.getApplication();
        final boolean isIssued = application.getIssueDate() != null;

        final List<String> caseValidationErrors = validateChangeServiceRequest(details.getData());

        if (!isEmpty(caseValidationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(caseValidationErrors)
                .build();
        }

        State state = details.getState();

        if (isIssued) {
            log.info("Regenerate NOP for App and Respondent, and D10 for case id: {}", details.getId());
            details = caseTasks(generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings, generateD10Form).run(details);
            state = application.isCourtServiceMethod() ? AwaitingAos : AwaitingService;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
