package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class HelpWithFeesPage implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("HelpWithFees")
            .pageLabel("Help with fees")
            .showCondition("solPaymentHowToPay=\"feesHelpWith\"")
            .complex(CaseData::getApplication)
                .complex(Application::getApplicant1HelpWithFees)
                    .mandatoryWithLabel(HelpWithFees::getReferenceNumber, "Applicant 1 help with fees reference")
                .done()
            .complex(Application::getApplicant2HelpWithFees)
                .mandatory(HelpWithFees::getReferenceNumber, "applicationType=\"jointApplication\"",
                null, "Applicant 2 help with fees reference")
                .done()
            .done();
    }
}
