package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class SolSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("SolSummary")
            .label("LabelSolAppSummaryPara-1",
                "# Before you submit\n\n"
                    + "## What happens next\n\n"
                    + "### Please continue to submit your application on the next screen.\n\n"
                    + "The application will be checked. If it’s correct, you’ll be sent a notice of issue. "
                    + "${labelContentTheApplicant2} will also receive a copy of the application unless you have chosen to personally "
                    + "effect service.\n\n"
                    + "Contact the divorce centre if you don't hear anything back after 3 weeks.\n"
                    + "- Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)\n"
                    + "- Email: contactdivorce@justice.gov.uk\n\n"
                    + "## Help us improve this service\n\n"
                    + "This is a new service that is still being developed. "
                    + "If you haven't already done so, please provide feedback on what you think of it and how it can be improved.\n\n"
                    + "## If you need help\n\n"
                    + "You can contact the divorce centre if you need help with your application.\n"
                    + "- Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)\n"
                    + "- Email: contactdivorce@justice.gov.uk");
    }
}
