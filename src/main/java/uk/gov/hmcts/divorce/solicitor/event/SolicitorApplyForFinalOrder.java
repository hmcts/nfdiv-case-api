package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SolicitorApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {
    public static final String SOLICITOR_FINAL_ORDER_REQUESTED = "solicitor-final-order-requested";

    private static final String ALWAYS_HIDE = "doesApplicantWantToApplyForFinalOrder=\"ALWAYS_HIDE\"";
    private static final String APPLY_FOR_FINAL_ORDER = "Apply for final order";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_FINAL_ORDER_REQUESTED)
            .forStateTransition(AwaitingFinalOrder, FinalOrderRequested)
            .name(APPLY_FOR_FINAL_ORDER)
            .description(APPLY_FOR_FINAL_ORDER)
            .showSummary()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR))
            .page("SolicitorApplyForFinalOrder", this::midEvent)
            .pageLabel(APPLY_FOR_FINAL_ORDER)
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getFinaliseDivorceOrEndCivilPartnership, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .readonlyNoSummary(MarriageDetails::getApplicant1Name, ALWAYS_HIDE)
                    .readonlyNoSummary(MarriageDetails::getApplicant2Name, ALWAYS_HIDE)
                .done()
            .done()
            .label("label-ApplyForFinalOrder",
                "Once the court has checked that there are no outstanding applications "
                    + "or other matters which need to be resolved first, the final order will be made absolute"
                    + " and the ${labelContentDivorceOrCivilPartnership} between ${marriageApplicant1Name} and "
                    + " ${marriageApplicant2Name} will be ended.")
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getDoesApplicantWantToApplyForFinalOrder)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolicitorApplyForFinalOrder");

        final CaseData data = details.getData();

        if (!data.getFinalOrder().getDoesApplicantWantToApplyForFinalOrder().toBoolean()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(singletonList("You must select 'Yes' to apply for Final Order"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
