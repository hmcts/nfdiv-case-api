package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class HelpWithFeesPageForApplicant2SolFinalOrder implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("HelpWithFeesForApplicant2SolFinalOrder")
            .pageLabel("Help with fees")
            .showCondition("applicant2SolPaymentHowToPay=\"feesHelpWith\"")
            .complex(CaseData::getFinalOrder)
                .complex(FinalOrder::getApplicant2SolFinalOrderHelpWithFees)
                    .mandatory(
                        HelpWithFees::getReferenceNumber,
                        "applicant2SolPaymentHowToPay=\"feesHelpWith\"",
                        null,
                        "Respondent help with fees reference"
                    )
                .done()
            .done();
    }
}
