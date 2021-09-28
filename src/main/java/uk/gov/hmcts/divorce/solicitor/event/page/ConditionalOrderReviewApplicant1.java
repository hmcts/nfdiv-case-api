package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

public class ConditionalOrderReviewApplicant1 implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewApplicant1")
            .pageLabel("Review the applicant's application - Draft Conditional Order Application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getChangeOrAddToApplication)
                .mandatory(ConditionalOrder::getIsEverythingInPetitionTrue)
            .done();

    }
}
