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
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class Applicant2SolicitorSwitchedToSoleCo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO = "app2-sol-switch-to-sole-co";

    @Autowired
    private SwitchToSoleService switchToSoleService;

    @Autowired
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO)
            .forStateTransition(ConditionalOrderPending, AwaitingLegalAdvisorReferral)
            .showCondition("coApplicant2EnableSolicitorSwitchToSoleCo=\"Yes\"")
            .name("App2SwitchedToSoleCO")
            .description("Application type switched to sole post CO submission")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit))
            .page("app2SolSwitchToSoleCo")
            .pageLabel("Changing to a sole conditional order application")
            .label("app2SolSwitchToSoleLabel1", "This case is a joint application.")
            .label("app2SolSwitchToSoleLabel2",
                """
                    If you change to a sole conditional order application, then the other party will become the respondent.
                    You will not be able to change back to a joint application after you have applied.
                    The other party will be notified of this change.
                    """
            )
            .complex(CaseData::getConditionalOrder)
            .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
            .mandatory(ConditionalOrderQuestions::getConfirmSwitchToSole);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        Long caseId = details.getId();
        log.info("SolicitorSwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YES);
        data.getLabelContent().setApplicationType(SOLE_APPLICATION);
        data.getConditionalOrder().setSwitchedToSole(YES);

        if (data.getApplicant1().isRepresented()) {
            switchToSoleService.switchSolicitorUserRoles(caseId);
        } else {
            switchToSoleService.switchSolicitorAndCitizenUserRoles(caseId);
        }

        switchToSoleService.switchApplicantData(data);

        generateConditionalOrderAnswersDocument.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
