package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorSwitchToSoleCoNotification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;

@Slf4j
@RequiredArgsConstructor
@Component
public class Applicant2SolicitorSwitchToSoleCo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO = "app2-sol-switch-to-sole-co";

    private final SwitchToSoleService switchToSoleService;
    private final NotificationDispatcher notificationDispatcher;
    private final SolicitorSwitchToSoleCoNotification solicitorSwitchToSoleCoNotification;
    private final DocumentGenerator documentGenerator;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO)
            .forStates(ConditionalOrderPending, JSAwaitingLA, AwaitingLegalAdvisorReferral)
            .showCondition("coApplicant2EnableSolicitorSwitchToSoleCo=\"Yes\"")
            .name("Switch To Sole CO")
            .description("Changing to a sole conditional order application")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR)
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
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
                    .mandatory(ConditionalOrderQuestions::getConfirmSwitchToSole)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();
        Long caseId = details.getId();

        log.info("Applicant 2 Solicitor SwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);

        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YES);
        data.getLabelContent().setApplicationType(SOLE_APPLICATION);
        data.getConditionalOrder().setSwitchedToSole(YES);

        switchToSoleService.switchUserRoles(data, caseId);
        switchToSoleService.switchApplicantData(data);

        // NOTE: Applicant 2 is now Applicant 1
        documentGenerator.generateAndStoreCaseDocument(
            CONDITIONAL_ORDER_ANSWERS,
            CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID,
            CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME,
            data,
            caseId,
            data.getApplicant1()
        );

        var state = details.getState() == JSAwaitingLA ? JSAwaitingLA : AwaitingLegalAdvisorReferral;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("Applicant 2 Solicitor SwitchedToSoleCO submitted callback invoked for case id: {}", details.getId());

        notificationDispatcher.send(solicitorSwitchToSoleCoNotification, details.getData(), details.getId());
        return SubmittedCallbackResponse.builder().build();
    }
}
