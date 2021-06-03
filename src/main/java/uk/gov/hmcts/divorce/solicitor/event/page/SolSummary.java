package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolSummary")
            .label(
                "LabelNFDBanner-SolSummary",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolSummary",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label("LabelSolAppSummaryPara-1",
                "<h1>Before you submit</h1>"
                    + "<h2>What happens next</h2>"
                    + "<h3>Please continue to submit your application on the next screen.</h3>"
                    + "The application will be checked. If it’s correct, you’ll be sent a notice of issue. "
                    + "Applicant 2 will also receive a copy of the application unless you have chosen to personally effect service.<br />"
                    + "Contact the divorce centre if you don't hear anything back after 3 weeks.<br />"
                    + "• Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)<br />"
                    + "• Email: contactdivorce@justice.gov.uk<br /><br />"
                    + "<h2>Help us improve this service</h2>"
                    + "This is a new service that is still being developed. "
                    + "If you haven't already done so, please provide feedback on what you think of it and how it can be improved.<br />"
                    + "<h2>If you need help</h2>"
                    + "You can contact the divorce centre if you need help with your application.<br />"
                    + "• Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)<br />"
                    + "• Email: contactdivorce@justice.gov.uk<br /><br />");
    }
}
