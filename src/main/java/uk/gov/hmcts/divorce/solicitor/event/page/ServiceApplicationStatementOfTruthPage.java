package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class ServiceApplicationStatementOfTruthPage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("serviceApplicationStatementOfTruth")
            .pageLabel("Submit service application")
            .complex(CaseData::getAlternativeService)
                .readonlyNoSummary(AlternativeService::getAlternativeServiceType)
                .mandatory(AlternativeService::getServiceApplicationStatementOfTruth)
                .mandatory(AlternativeService::getServiceApplicationSignStatementOfTruth)
                .label("LabelServiceApp-SOTInfo",
                    "This confirms that the information you are submitting on behalf of the applicant is true and accurate, "
                        + "to the best of your knowledge. It’s known as the ‘statement of truth’.")
                .mandatory(AlternativeService::getServiceApplicationStatementOfTruthSolsName)
                .mandatory(AlternativeService::getServiceApplicationStatementOfTruthSolsFirm)
                .label("LabelSolStatementOfTruth-Comments",
                    "If you have any comments you would like to make to the court staff regarding the application you "
                        + "may include them below.")
                .optionalNoSummary(AlternativeService::getServiceApplicationStatementOfTruthComments)
            .done();
    }
}
