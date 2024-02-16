package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;

public class SolFinalOrderPayAccount implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolFinalOrderPayAccount")
            .pageLabel("Pay on account")
            .showCondition("applicant2SolPaymentHowToPay=\"feePayByAccount\"")
            .complex(CaseData::getFinalOrder)
                .mandatoryWithLabel(FinalOrder::getFinalOrderPbaNumbers, "Select your account number")
                .mandatoryWithLabel(FinalOrder::getApplicant2SolFinalOrderFeeAccountReference, "Enter your payment reference")
            .done();
    }
}
