package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class FinancialOrders implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("FinancialOrders")
            .pageLabel("Financial orders")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFinancialOrder,
                    "Does ${labelContentTheApplicantOrApplicant1} wish to apply for a financial order?")
                .mandatory(Applicant::getFinancialOrdersFor, "applicant1FinancialOrder=\"Yes\"")
                .done();
    }
}
