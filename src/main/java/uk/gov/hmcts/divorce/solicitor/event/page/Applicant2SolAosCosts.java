package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class Applicant2SolAosCosts implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosCosts")
            .pageLabel("Paying for the divorce")
            .label("LabelRespSol-AOSCost",
                "The applicant 1 asked the court that the respondent pays some or all of the costs of the divorce.\n\n"
                    + "It is up to the court to decide how the costs will be split, but it will take into account "
                    + "any requests the respondent and the applicant 1  make.")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getAgreeToCosts)
            .mandatory(AcknowledgementOfService::getCostsAmount,
                "agreeToCosts=\"DifferentAmount\"")
            .mandatory(AcknowledgementOfService::getCostsReason,
                "agreeToCosts=\"No\" OR agreeToCosts=\"DifferentAmount\"")
            .done();
    }
}
