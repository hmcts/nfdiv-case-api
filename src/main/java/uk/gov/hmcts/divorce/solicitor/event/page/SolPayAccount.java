package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class SolPayAccount implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("SolPayAccount")
            .pageLabel("Pay account")
            .showCondition("solPaymentHowToPay=\"feePayByAccount\"")
            .complex(CaseData::getApplication)
                .optional(Application::getPbaNumbers)
                .mandatory(Application::getFeeAccountReference)
                .done();
    }
}
