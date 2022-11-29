package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.ApplyForFinalOrder.APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class Applicant2SolicitorSwitchToSoleFo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_FO = "app2-sol-switch-to-sole-fo";

    @Autowired
    private SwitchToSoleService switchToSoleService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_FO)
            .forStateTransition(AwaitingJointFinalOrder, FinalOrderRequested)
            .showCondition("applicationType=\"jointApplication\""
                + " AND dateApplicant2DeclaredIntentionToSwitchToSoleFo=\"*\""
                + " AND finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention=\"Yes\"")
            .name("Switched to sole final order")
            .description("Switched to sole final order")
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(LEGAL_ADVISOR, APPLICANT_1_SOLICITOR)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit))
            .page("App2SolSwitchToSoleFO", this::midEvent)
            .pageLabel(APPLY_FOR_FINAL_ORDER)
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getDoesApplicant2WantToApplyForFinalOrder)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for App2SolSwitchToSoleFO");

        final CaseData data = details.getData();
        if (NO.equals(data.getFinalOrder().getDoesApplicant2WantToApplyForFinalOrder())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(singletonList("You must select 'Yes' to apply for Final Order"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        CaseData caseData = details.getData();

        caseData.getFinalOrder().setDoesApplicant2WantToApplyForFinalOrder(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();
        log.info("Applicant 2 Solicitor Switched To Sole FO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData caseData = details.getData();

        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getLabelContent().setApplicationType(SOLE_APPLICATION);
        caseData.getFinalOrder().setFinalOrderSwitchedToSole(YES);

        switchToSoleService.switchUserRoles(caseData, caseId);
        switchToSoleService.switchApplicantData(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
