package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.common.event.Applicant2ApplyForFinalOrder.APPLICANT2_APPLY_FOR_FINAL_ORDER;

@Slf4j
public class Applicant2SolApplyForFinalOrderDetails implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "doesApplicant2WantToApplyForFinalOrder=\"ALWAYS_HIDE\"";
    private static final String APPLY_FOR_FINAL_ORDER_RESPONDENT_SOL_GUIDE = "Refer to the <a href=\"https://www.gov"
            + ".uk/government/publications/myhmcts-how-to-make-follow-up-applications-for-a-divorce-or-dissolution/"
            + "25890a91-44e6-4264-b868-100afb2e8544\" target=\"_blank\" rel=\"noopener noreferrer\">Solicitor Guidance</a>";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolicitorApplyForFinalOrder", this::midEvent)
            .pageLabel(APPLICANT2_APPLY_FOR_FINAL_ORDER)
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicant2, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getFinaliseDivorceOrEndCivilPartnership, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .readonlyNoSummary(MarriageDetails::getApplicant1Name, ALWAYS_HIDE)
                    .readonlyNoSummary(MarriageDetails::getApplicant2Name, ALWAYS_HIDE)
                .done()
            .done()
            .complex(CaseData::getFinalOrder)
                .mandatoryWithLabel(
                    FinalOrder::getDoesApplicant2WantToApplyForFinalOrder,
                "Does the respondent want to apply for a final order?"
                )
            .done()
                .label("applyForFinalOrderRespondentSolGuide", APPLY_FOR_FINAL_ORDER_RESPONDENT_SOL_GUIDE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for Applicant2SolicitorApplyForFinalOrder");

        final CaseData data = details.getData();

        if (!data.getFinalOrder().getDoesApplicant2WantToApplyForFinalOrder().toBoolean()) {
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
