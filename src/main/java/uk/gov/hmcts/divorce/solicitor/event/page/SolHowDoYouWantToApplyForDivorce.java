package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolHowDoYouWantToApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("howDoYouWantToApplyForDivorce")
            .pageLabel("How do you want to apply for the divorce?")
            .label("solHowDoYouWantToApplyForDivorcePara-1", "Applicant 1 can apply for the divorce on their"
                + " own (as a 'sole applicant') or with their husband or wife (in a 'joint application').")
            .label("solHowDoYouWantToApplyForDivorceHeader-1", "### Applying as a sole applicant")
            .label("solHowDoYouWantToApplyForDivorcePara-2", "If Applicant 1 applies as a sole applicant,"
                + " Applicant 1's husband or wife responds to the divorce application after you have submitted it."
                + " Applicant 1 will be applying on their own.")
            .label("solHowDoYouWantToApplyForDivorceHeader-2", "### Applying jointly, with applicant 1's "
                + "husband or wife")
            .label("solHowDoYouWantToApplyForDivorcePara-3", "If applicant 1 applies jointly, "
                + " applicant 1's husband or wife joins and reviews this online application before it's submitted."
                + " They will be applying together.")
            .label("solHowDoYouWantToApplyForDivorcePara-4", "*How applicant 1 divides their money"
                + " and property is dealt with separately. It should  not affect the decision on whether to do a sole"
                + " or a joint application.*")
            .mandatory(CaseData::getApplicationType)
            .mandatory(CaseData::getDivorceOrDissolution);
    }
}
