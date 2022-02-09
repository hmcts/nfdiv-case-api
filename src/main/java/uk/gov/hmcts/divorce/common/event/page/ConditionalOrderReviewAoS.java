package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ConditionalOrderReviewAoS implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewAoS", this::midEvent)
            .pageLabel("Review Acknowledgement of Service - Draft Conditional Order Application")
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getRespondentAnswersLink)
            .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                .mandatory(ConditionalOrderQuestions::getApplyForConditionalOrder)
                .done()
                .label(
                    "ConditionalOrderReviewAoSNo",
                    "You must select yes to be able to draft a conditional order",
                    "coApplicant1ApplyForConditionalOrder=\"No\""
                )
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for ConditionalOrderReviewAoS");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        // TODO: Review error message to get actual text
        if (!conditionalOrder.getConditionalOrderApplicant1Questions().getApplyForConditionalOrder().toBoolean()) {
            errors.add("To continue, applicant 1 must select Yes");
        }

        if (conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrder() != null &&
            !conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrder().toBoolean()) {
            errors.add("To continue, applicant 2 must select Yes");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
