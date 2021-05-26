package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolPayAccount implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPayAccount")
            .pageLabel("Pay account")
            .showCondition("solPaymentHowToPay=\"feePayByAccount\"")
            .optional(CaseData::getPbaNumbers)
            .mandatory(CaseData::getFeeAccountReference);
    }
}
