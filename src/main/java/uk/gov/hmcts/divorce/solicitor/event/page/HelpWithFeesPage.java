package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class HelpWithFeesPage implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("HelpWithFees")
            .pageLabel("Help with fees")
            .showCondition("solPaymentHowToPay=\"feesHelpWith\"")
            .complex(CaseData::getApplication)
                .complex(Application::getApplicant1HelpWithFees)
                    .mandatory(HelpWithFees::getReferenceNumber)
                    .done()
                .done();
    }
}
