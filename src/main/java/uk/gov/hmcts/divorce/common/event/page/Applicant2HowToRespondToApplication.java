package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class Applicant2HowToRespondToApplication implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("applicant2HowToResponseToApplication")
            .pageLabel("How does the applicant want to respond to the application?")
            .label("label-ApplicantDispute", "The applicant can only dispute the application if: (one or more of the following)\n\n")
            .label("label-HaveLegalPowerBullet",
                "- they do not think the courts of England and Wales have the legal power (jurisdiction) to grant the application")
            .label("label-DoNotBelieveMarriageValidBullet",
                "- they do not believe their marriage is legally valid. For example, if one of them was already married or in a civil partnership when they got married")
            .label("label-MarriageLegallyEndedBullet",
                "- the marriage has already been legally ended")
            .complex(CaseData::getAcknowledgementOfService)
                .mandatory(AcknowledgementOfService::getHowToRespondApplication)
                .done()
            .done();
    }
}
