package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;

public class ConditionalOrderReviewApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewApplicant2")
            .pageLabel("Review the applicant's application - Draft Conditional Order Application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
            .mandatory(ConditionalOrderQuestions::getConfirmInformationStillCorrect)
            .mandatory(ConditionalOrderQuestions::getReasonInformationNotCorrect,
                "coApplicant2ConfirmInformationStillCorrect=\"No\"")
                .done()
            .done();
    }
}
