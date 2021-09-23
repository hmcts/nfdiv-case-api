package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

public class ConditionalOrderReviewAdultery implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("ConditionalOrderReviewAdultery")
            .pageLabel("Adultery - Draft Conditional Order Application")
            .complex(CaseData::getApplication)
                .complex(Application::getConditionalOrder)
                    .mandatory(ConditionalOrder::getDoesApplicantFindItIntolerable)
                    .mandatory(ConditionalOrder::getDateOfAdulteryKnowledge, "conOrderDoesApplicantFindItIntolerable=\"Yes\"")
                    .mandatory(ConditionalOrder::getLivedApartFrom, "conOrderDoesApplicantFindItIntolerable=\"Yes\"")
                    .mandatory(ConditionalOrder::getAdulteryAdditionalDetails, "conOrderDoesApplicantFindItIntolerable=\"Yes\"")
                .done();
    }
}
