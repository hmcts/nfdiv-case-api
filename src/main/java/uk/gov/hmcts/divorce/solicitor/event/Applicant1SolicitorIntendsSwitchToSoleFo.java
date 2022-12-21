package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class Applicant1SolicitorIntendsSwitchToSoleFo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO = "applicant1-intends-switch-to-sole-fo";
    public static final String INTEND_TO_SWITCHED_TO_SOLE_FO_ERROR =
        "You have not answered the question. You need to select an answer before continuing.";
    private static final String NEVER_SHOW = "applicant1IntendsToSwitchToSole=\"NEVER_SHOW\"";
    private static final String BLANK_LABEL = " ";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Autowired
    private Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO)
            .forStates(AwaitingJointFinalOrder)
            .name("Intends to switch to sole FO")
            .description("Applicant 1 Intention to apply for a final order")
            .showCondition("applicationType=\"jointApplication\""
                + " AND applicant1CanIntendToSwitchToSoleFo=\"Yes\""
                + " AND doesApplicant1IntendToSwitchToSole=\"No\""
            )
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CITIZEN, CASE_WORKER, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
            .page("app1IntentionSwitchToSoleFo", this::midEvent)
            .pageLabel("Intention to apply for a final order")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getFinaliseDivorceOrLegallyEndYourCivilPartnership, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant2)
                .readonlyNoSummary(Applicant::getSolicitorRepresented, NEVER_SHOW)
            .done()
            .label("app1OtherApplicantIsRepresented",
                getOtherApplicantIsRepresentedLabel(), "applicant2SolicitorRepresented=\"Yes\"")
            .label("app1OtherApplicantIsNotRepresented",
                getOtherApplicantIsNotRepresentedLabel(),"applicant2SolicitorRepresented=\"No\"")
            .label("app1IntendsSwitchToSoleFoInfo", getIntendsToSwitchToSoleInformationLabel())
            .complex(CaseData::getFinalOrder)
                .optionalNoSummary(FinalOrder::getApplicant1IntendsToSwitchToSole, null, BLANK_LABEL)
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final CaseData data = details.getData();

        if (isEmpty(data.getFinalOrder().getApplicant1IntendsToSwitchToSole())
            || !data.getFinalOrder().getApplicant1IntendsToSwitchToSole().contains(I_INTEND_TO_SWITCH_TO_SOLE)
        ) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(INTEND_TO_SWITCHED_TO_SOLE_FO_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        data.getFinalOrder().setDoesApplicant1IntendToSwitchToSole(YES);
        data.getFinalOrder().setDateApplicant1DeclaredIntentionToSwitchToSoleFo(LocalDate.now(clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }


    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("Applicant1SolicitorIntendsSwitchToSoleFo submitted callback invoked for case id: {}", details.getId());

        notificationDispatcher.send(solicitorIntendsToSwitchToSoleFoNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }

    public static String getOtherApplicantIsRepresentedLabel() {
        return """
                The quickest way to ${labelContentFinaliseDivorceOrLegallyEndYourCivilPartnership}
                is for the other applicant’s solicitor to confirm the joint application for a final order.
                They have been emailed details of how to do this.
                """;
    }


    public static String getOtherApplicantIsNotRepresentedLabel() {
        return """
                The quickest way to finalise the ${labelContentFinaliseDivorceOrLegallyEndYourCivilPartnership}
                is for the other applicant to confirm the joint application for a final order.
                They have been emailed details of how to do this.
                """;
    }

    public static String getIntendsToSwitchToSoleInformationLabel() {
        return """
                If they will not confirm then you can apply for a final order as a sole applicant, on behalf of your client.
                First the other applicant needs to be given 2 week’s notice of the intention to apply.
                """;
    }
}
