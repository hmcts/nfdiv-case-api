package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class FinancialOrders implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("FinancialOrders")
            .pageLabel("Financial orders")
            .label(
                "financialOrdersEditMessage",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getFinancialOrder)
            .mandatory(
                CaseData::getFinancialOrderFor,
                "financialOrder=\"Yes\"");
    }
}
