package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class ClaimForCosts implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("ClaimForCosts")
            .pageLabel("Claim for costs")
            .label(
                "claimForCostsEditMessage",
                "You can make changes at the end of your application.")
            .label(
                "LabelClaimForCostsPara-1",
                "A claim for costs can include all the fees applicant 1 has to pay during the divorce, such as "
                    + "application fees, solicitor fees and any extra court fees.")
            .mandatory(CaseData::getDivorceCostsClaim);
    }
}
