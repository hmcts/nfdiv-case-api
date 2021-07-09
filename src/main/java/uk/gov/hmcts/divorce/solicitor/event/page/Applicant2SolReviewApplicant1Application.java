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
                    + "# Respondent\n"
                    + "${applicant2FirstName} ${applicant2LastName}\n\n"
                    + "You must respond to this application within 7 days of the respondent receiving the letter from the courts."
                    + " If you don't, the applicant 1 can ask the court to move the divorce forwards without the respondent. "
                    + "The respondent may have to pay extra fees if this happens\n\n"
                    + "If the respondent has been married to more than one person during this marriage (polygamy) contact "
                    + "your regional divorce centre before you apply.")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getConfirmReadPetition)
            .label("LabelRespSol-AOSConfirmRead",
                "### The respondent has not read the petition\n\n"
                    + "The respondent must have read the petition in order to respond.",
              "confirmReadPetition=\"No\"")
            .done();
    }
}
