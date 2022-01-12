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
                    .mandatory(HelpWithFees::getReferenceNumber)
                    .done()
            .done()
            .label("LabelHWFPage-Applicant2HWFRef",
                "applicationType=\"jointApplication\" AND applicant2HWFReferenceNumber=\"*\"",
                "**Applicant 2 Help with fee reference:**  \n**£${applicant2HWFReferenceNumber}**");
    }
}
