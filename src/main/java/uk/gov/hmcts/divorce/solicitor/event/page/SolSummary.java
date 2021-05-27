package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;

public class SolSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolSummary")
            .label("LabelSolAppSummaryPara-1",
                "# Before you submit # <br />"
                + "## What happens next ## <br />"
                + "### Please continue to submit your application on the next screen. ### <br />"
                + "The application will be checked. If it’s correct, you’ll be sent a notice of issue. "
                + "Applicant 2 will also receive a copy of the application unless you have chosen to personally effect service.<br />"
                + "Contact the divorce centre if you don't hear anything back after 3 weeks.<br />"
                + "• Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)<br />"
                + "• Email: contactdivorce@justice.gov.uk<br /><br />"
                + "## Help us improve this service ## <br />"
                + "This is a new service that is still being developed. "
                + "If you haven't already done so, please provide feedback on what you think of it and how it can be improved.<br />"
                + "## If you need help ## <br />"
                + "You can contact the divorce centre if you need help with your application.<br />"
                + "• Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)<br />"
                + "• Email: contactdivorce@justice.gov.uk");
    }
}
