package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class HelpWithFeesPageForApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("HelpWithFees")
            .pageLabel("Help with fees")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2NeedsHelpWithFees)
                .complex(Application::getApplicant2HelpWithFees)
                    .mandatory(HelpWithFees::getReferenceNumber,"applicant2NeedsHelpWithFees=\"Yes\"")
                    .done()
                .done();
    }
}
