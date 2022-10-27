package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class Applicant1SolicitorIntendsSwitchToSoleFo implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO = "applicant1-intends-switch-to-sole-fo";
    private static final String BLANK_LABEL = "";

    @Autowired
    private Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO)
            .forStates(AwaitingJointFinalOrder)
            .name("Intends to switch to sole FO")
            .description("Applicant 1 intends to switch to sole FO")
            .showCondition("applicationType=\"jointApplication\" AND applicant1CanIntendToSwitchToSoleFo\"Yes\"")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CITIZEN, CASE_WORKER, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit))
            .page("app1IntentionSwitchToSoleFo")
            .pageLabel("Intention to apply for a final order")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getFinaliseDivorceOrLegallyEndYourCivilPartnership, NEVER_SHOW)
            .done()
            .label("app1OtherApplicantIsRepresented",
                getOtherApplicantIsRepresentedLabel(), "applicant2SolicitorRepresented=\"Yes\"")
            .label("app1OtherApplicantIsNotRepresented",
                getOtherApplicantIsNotRepresentedLabel(),"applicant2SolicitorRepresented=\"No\"")
            .label("app1IntendsSwitchToSoleFoInfo", getIntendsToSwitchToSoleInformationLabel())
            .complex(CaseData::getFinalOrder)
                .mandatoryNoSummary(FinalOrder::getApplicant1IntendsToSwitchToSole, null, BLANK_LABEL)
            .done();
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

    public static String getOtherApplicantIsRepresentedLabel() {
        return """
                The quickest way to finalise the ${labelContentFinaliseDivorceOrLegallyEndYourCivilPartnership}
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
