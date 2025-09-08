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
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.generalApplicationLabels;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateGeneralApplicationList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PRE_RETURN_TO_PREVIOUS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerRejectGeneralApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REJECT_GENERAL_APPLICATION = "reject-general-application";
    private static final String REJECT_GENERAL_APPLICATION = "Reject general application";
    public static final String INVALID_STATE_ERROR
        = "You cannot move this case into a pre-submission state. Select another state before continuing.";
    public static final String CASE_MUST_BE_ISSUED_ERROR
        = "You cannot move this case into a post-issue state as it has not been issued";
    public static final String CASE_ALREADY_ISSUED_ERROR
        = "You cannot move this case into a pre-issue state as it has already been issued";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_REJECT_GENERAL_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .name(REJECT_GENERAL_APPLICATION)
            .description(REJECT_GENERAL_APPLICATION)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("rejectGeneralApplication", this::midEvent)
            .pageLabel(REJECT_GENERAL_APPLICATION)
            .complex(CaseData::getGeneralReferral)
            .mandatoryWithLabel(GeneralReferral::getSelectedGeneralApplication, "Which general application will be rejected?")
            .done()
            .complex(CaseData::getApplication)
                .readonly(Application::getCurrentState)
                .mandatoryWithLabel(Application::getStateToTransitionApplicationTo, "State to transfer case to")
            .done()
            .page("rejectGenReferral")
            .showCondition("generalReferralType=\"*\"")
            .pageLabel("Reject general referral")
            .complex(CaseData::getGeneralReferral)
                .readonly(GeneralReferral::getGeneralReferralType)
                .readonly(GeneralReferral::getGeneralReferralReason)
                .readonly(GeneralReferral::getGeneralApplicationFrom)
                .mandatory(GeneralReferral::getRejectGeneralReferral)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_REJECT_GENERAL_APPLICATION, details.getId());

        log.info("Retrieving active general applications for Case Id: {}", details.getId());
        final CaseData caseData = details.getData();

        populateGeneralApplicationList(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker rejected about to submit callback invoked: {}, Case Id: {}", details.getState(), details.getId());
        var caseData = details.getData();
        String generalApplicationSelected = caseData.getGeneralReferral().getSelectedGeneralApplication().getValue().getLabel();
        details.setState(caseData.getApplication().getStateToTransitionApplicationTo());

        generalApplicationLabels(caseData)
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(generalApplicationSelected))
            .map(Map.Entry::getKey)
            .findFirst()
            .ifPresent(index -> {

                GeneralApplication generalApplication = caseData.getGeneralApplications().get(index).getValue();
                String serviceRequestReference = generalApplication.getGeneralApplicationFee().getServiceRequestReference();

                Stream.of(caseData.getApplicant1(), caseData.getApplicant2())
                    .filter(applicant -> Objects.equals(applicant.getGeneralAppServiceRequest(), serviceRequestReference))
                    .findFirst()
                    .ifPresent(applicant -> applicant.setActiveGeneralApplication(null));

                caseData.getGeneralApplications().remove(index.intValue());
                details.setState(caseData.getApplication().getStateToTransitionApplicationTo());
                resetApplicationFields(caseData, details);
            });

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        State state = caseData.getApplication().getStateToTransitionApplicationTo();
        List<String> validationErrors = new ArrayList<>();

        if (!PRE_RETURN_TO_PREVIOUS_STATES.contains(state)) {
            validationErrors.add(INVALID_STATE_ERROR);
        }

        if (POST_ISSUE_STATES.contains(state) && caseData.getApplication().getIssueDate() == null) {
            validationErrors.add(CASE_MUST_BE_ISSUED_ERROR);
        } else if (EnumSet.complementOf(POST_ISSUE_STATES).contains(state) && caseData.getApplication().getIssueDate() != null) {
            validationErrors.add(CASE_ALREADY_ISSUED_ERROR);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationErrors)
            .build();
    }

    private void resetApplicationFields(CaseData caseData, CaseDetails<CaseData, State> details) {
        caseData.getGeneralReferral().setSelectedGeneralApplication(null);
        if (YesOrNo.YES.equals(caseData.getGeneralReferral().getRejectGeneralReferral())) {
            caseData.setGeneralReferral(null);
        }
        caseData.getApplication().setPreviousState(details.getState());
        caseData.getApplication().setStateToTransitionApplicationTo(null);
    }
}
