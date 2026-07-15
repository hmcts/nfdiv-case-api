package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.validateStateUpdate;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerDeleteGeneralReferral implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_DELETE_GENERAL_REFERRAL = "delete-general-referral";
    private static final String DELETE_GENERAL_REFERRAL = "Reject general referral";
    private static final String NO_GENERAL_REFERRAL_ERROR = "No general referral exists to delete.";
    private static final String WARNING_MESSAGE = "You are about to delete the general referral. This action cannot be undone.";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_DELETE_GENERAL_REFERRAL)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .name(DELETE_GENERAL_REFERRAL)
            .description(DELETE_GENERAL_REFERRAL)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE,
                SUPER_USER)
            .grantHistoryOnly(
                SOLICITOR,
                CASE_WORKER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("rejectGeneralReferral", this::midEvent)
            .pageLabel(DELETE_GENERAL_REFERRAL)
            .complex(CaseData::getGeneralReferral)
                .label("generalReferralTypeLabel", "## Note: The following general referral will be deleted")
                .readonlyNoSummary(GeneralReferral::getGeneralReferralType)
                .readonlyNoSummary(GeneralReferral::getGeneralReferralReason)
                .readonlyNoSummary(GeneralReferral::getGeneralApplicationFrom)
            .done()
            .complex(CaseData::getApplication)
            .readonlyNoSummary(Application::getCurrentState)
            .mandatoryWithLabel(Application::getStateToTransitionApplicationTo, "State to transfer case to")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked, Case Id: {}", CASEWORKER_DELETE_GENERAL_REFERRAL, details.getId());

        final CaseData caseData = details.getData();

        List<String> validationErrors = new ArrayList<>();

        if (ObjectUtils.isEmpty(caseData.getGeneralReferral().getGeneralReferralReason())) {
            validationErrors.add(NO_GENERAL_REFERRAL_ERROR);
        }

        caseData.getApplication().setCurrentState(details.getState());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationErrors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked, Case Id: {}", CASEWORKER_DELETE_GENERAL_REFERRAL, details.getId());

        final CaseData caseData = details.getData();
        State state =  caseData.getApplication().getStateToTransitionApplicationTo();

        caseData.getApplication().setPreviousState(details.getState());
        caseData.setGeneralReferral(GeneralReferral.builder().build());

        List<String> warnings = Collections.singletonList(WARNING_MESSAGE);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .warnings(warnings)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        log.info("{} midEvent callback invoked, Case Id: {}", CASEWORKER_DELETE_GENERAL_REFERRAL, details.getId());
        List<String> validationErrors = validateStateUpdate(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(validationErrors)
            .build();
    }
}
