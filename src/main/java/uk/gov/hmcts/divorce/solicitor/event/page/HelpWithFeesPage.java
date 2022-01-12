package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class HelpWithFeesPage implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant1HWFReferenceNumber=\"ALWAYS_HIDE\"";

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
            .complex(CaseData::getApplication)
                .complex(Application::getApplicant2HelpWithFees)
                .readonlyNoSummary(HelpWithFees::getReferenceNumber, ALWAYS_HIDE)
                .done()
            .done()
            .label("LabelHWFPage-Applicant2HWFRef",
                "**Applicant 2 Help with fee reference:**  \n**£${applicant2HWFReferenceNumber}**",
                "applicationType=\"jointApplication\" AND applicant2HWFReferenceNumber=\"*\"");
    }
}
