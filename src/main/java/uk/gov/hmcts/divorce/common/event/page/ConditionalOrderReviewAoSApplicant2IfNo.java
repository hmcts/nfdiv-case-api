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
public class ConditionalOrderReviewAoSApplicant2IfNo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("ConditionalOrderReviewAoSIfNoApp2", this::midEvent)
            .showCondition("applicationType=\"jointApplication\" AND coApplicant2ApplyForConditionalOrder=\"No\"")
            .complex(CaseData::getConditionalOrder)
            .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
            .mandatory(ConditionalOrderQuestions::getApplyForConditionalOrderIfNo)
            .done()
            .label(
                "ConditionalOrderReviewAoSNoApp2IfNo",
                "You must select yes to apply for a conditional order",
                "coApplicant2ApplyForConditionalOrderIfNo=\"No\""
            );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for ConditionalOrderReviewAoSIfNo");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (!conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrderIfNo().toBoolean()) {
            errors.add("Applicant must select yes to apply for a conditional order");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
