package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class ConditionalOrderReviewAoS implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewAoS")
            .pageLabel("Review Acknowledgement of Service - Draft Conditional Order Application")
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getRespondentAnswersLink)
                .mandatory(ConditionalOrder::getApplyForConditionalOrder)
            .done();
    }
}
