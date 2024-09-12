package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDwpResponse;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderRefused;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateIssue;

@Component
@Slf4j
public class CaseworkerReissueApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REISSUE_APPLICATION = "caseworker-reissue-application";
    public static final String BLANK_LABEL = " ";
    public static final String REISSUE_NOT_POSSIBLE_ERROR_MESSAGE =
        "Acknowledgement of Service has been submitted therefore this case cannot be reissued";
    public static final String REISSUE_ISSUE_ERROR_MESSAGE =
        "Case has not been issued therefore it cannot be reissued";

    @Autowired
    private ReIssueApplicationService reIssueApplicationService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REISSUE_APPLICATION)
            .forStates(
                AwaitingAos, AosDrafted, AosOverdue, ConditionalOrderRefused,
                Holding, AwaitingDocuments, AwaitingService, AwaitingDwpResponse, AwaitingJsNullity)
            .name("Reissue")
            .description("Application reissued")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                SYSTEMUPDATE,
                JUDGE))
            .page("reissueApplication")
            .pageLabel("Reissue Application")
            .readonlyNoSummary(CaseData::getSupplementaryCaseType, "serviceMethod=\"NEVER_SHOW\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getReissueOption, "supplementaryCaseType=\"notApplicable\"")
                .mandatory(Application::getJudicialSeparationReissueOption, "supplementaryCaseType!=\"notApplicable\"")
                .mandatoryWithoutDefaultValue(Application::getServiceMethod,
                    "reissueOption=\"reissueCase\" OR judicialSeparationReissueOption=\"reissueCase\"", BLANK_LABEL, true)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Caseworker reissue application about to start callback invoked for case id: {}", details.getId());
        CaseData caseData = details.getData();
        List<String> validationErrors = new ArrayList<>();

        if (caseData.getAcknowledgementOfService() != null && caseData.getAcknowledgementOfService().getDateAosSubmitted() != null) {
            log.info("Reissue validation failed because AOS has already been submitted. Case ID: {}", details.getId());
            validationErrors.add(REISSUE_NOT_POSSIBLE_ERROR_MESSAGE);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        if (caseData.getApplication().getIssueDate() == null) {
            log.info("Reissue event not available because issue date is null. Case ID: {}", details.getId());
            validationErrors.add(REISSUE_ISSUE_ERROR_MESSAGE);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        log.info("Caseworker reissue application about to submit callback invoked for case id: {}", details.getId());

        log.info("Validating Issue for Case Id: {}", details.getId());
        final List<String> caseValidationErrors = validateIssue(details.getData());

        if (!isEmpty(caseValidationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(caseValidationErrors)
                .build();
        }

        try {
            final CaseDetails<CaseData, State> result = reIssueApplicationService.process(details);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(result.getData())
                .state(result.getState())
                .build();
        } catch (final InvalidReissueOptionException e) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of("Invalid reissue option, browser page refresh may have occurred. "
                    + "Please use 'Previous' button and select a reissue option"))
                .build();
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {

        ReissueOption reissueOption = details.getData().getApplication().getPreviousReissueOption();

        log.info("Caseworker reissue application submitted callback invoked for case id: {}, with reissue option - {}",
            details.getId(), reissueOption);

        reIssueApplicationService.sendNotifications(details, reissueOption);

        return SubmittedCallbackResponse.builder().build();
    }
}
