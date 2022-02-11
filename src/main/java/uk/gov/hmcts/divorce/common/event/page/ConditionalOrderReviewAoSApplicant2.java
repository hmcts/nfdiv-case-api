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
public class ConditionalOrderReviewAoSApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewAoSApplicant2", this::midEvent)
            .pageLabel("Review Acknowledgement of Service - Draft Conditional Order Application")
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getRespondentAnswersLink)
            .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
                .mandatory(ConditionalOrderQuestions::getApplyForConditionalOrder)
                .done()
                .label(
                    "ConditionalOrderReviewAoSNo",
                    "You must select yes to apply for a conditional order",
                    "coApplicant2ApplyForConditionalOrder=\"No\""
                )
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for ConditionalOrderReviewAoSApplicant2");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (!conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrder().toBoolean()) {
            errors.add("Applicant must select yes to apply for a conditional order");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
