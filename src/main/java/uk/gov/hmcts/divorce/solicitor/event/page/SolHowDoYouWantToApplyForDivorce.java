package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolHowDoYouWantToApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("howDoYouWantToApplyForDivorce")
            .pageLabel("How do you want to apply for the divorce?")
            .label("LabelNFDBanner-ApplyForDivorce", SOLICITOR_NFD_PREVIEW_BANNER)
            .label("solHowDoYouWantToApplyForDivorcePara-1",
                "The applicant can apply for the divorce on their own (as a 'sole applicant') or with their husband "
                    + "or wife (in a 'joint application').\n\n"
                    + "### Applying as a sole applicant\n\n"
                    + "If the applicant applies as a sole applicant, the applicant's husband or wife responds to the divorce "
                    + "application after you have submitted it.  The applicant will be applying on their own.\n\n"
                    + "### Applying jointly, with the applicant's husband or wife\n\n"
                    + "If the applicant applies jointly, the applicant's husband or wife joins and reviews this online "
                    + "application before it's submitted. They will be applying together.\n\n"
                    + "*How the applicant divides their money and property is dealt with separately. It should not affect "
                    + "the decision on whether to do a sole or a joint application.*")
            .mandatory(CaseData::getApplicationType, null, null,
                "How does the applicant want to apply for the divorce?",
                "The respondent must agree with a joint application in its entirety.")
            .mandatory(CaseData::getDivorceOrDissolution, null, null,
                "Is the application for a divorce or dissolution?");
    }
}
