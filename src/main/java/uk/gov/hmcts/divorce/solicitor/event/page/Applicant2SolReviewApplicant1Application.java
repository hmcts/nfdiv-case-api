package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class Applicant2SolReviewApplicant1Application implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolReviewApplicant1Application")
            .pageLabel("Review the applicant 1's application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .label("LabelRespSol-AOSRespond",
                "### Respond to a divorce application\n\n"
                    + "# Reference number\n"
                    + "${[CASE_REFERENCE]}\n\n"
                    + "# ${labelContentApplicant2UC}\n"
                    + "${applicant2FirstName} ${applicant2LastName}\n\n"
                    + "You must respond to this application within 7 days of ${labelContentTheApplicant2} receiving the letter "
                    + "from the courts."
                    + " If you don't, the applicant 1 can ask the court to move the divorce forwards without ${labelContentTheApplicant2}. "
                    + "${labelContentTheApplicant2} may have to pay extra fees if this happens\n\n"
                    + "If ${labelContentTheApplicant2} has been married to more than one person during this marriage (polygamy) contact "
                    + "your regional divorce centre before you apply.")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getConfirmReadPetition)
            .label("LabelRespSol-AOSConfirmRead",
                "### ${labelContentTheApplicant2} has not read the petition\n\n"
                    + "${labelContentTheApplicant2} must have read the petition in order to respond.",
              "confirmReadPetition=\"No\"")
            .done();
    }
}
