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
public class Applicant2ApplyForFinalOrderDetails implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "doesApplicant2WantToApplyForFinalOrder=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolicitorApplyForFinalOrder", this::midEvent)
            .pageLabel(APPLICANT2_APPLY_FOR_FINAL_ORDER)
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
            .complex(CaseData::getFinalOrder)
            .mandatory(FinalOrder::getDoesApplicant2WantToApplyForFinalOrder)
            .done();
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
