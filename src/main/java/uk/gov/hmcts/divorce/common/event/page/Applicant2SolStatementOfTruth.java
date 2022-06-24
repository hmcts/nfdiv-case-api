package uk.gov.hmcts.divorce.common.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

@Component
public class Applicant2SolStatementOfTruth implements CcdPageConfiguration {
    private static final String ALWAYS_HIDE = "jurisdictionAgree=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("Applicant2SolStatementOfTruth")
            .pageLabel("Statement of truth")
            .label("LabelApplicant2SolStatementOfTruth-AoSReview", "### Review the answers in your Acknowledgement "
                + "of Service below. If you wish to change any of your answers, please go back and use the 'Update AoS' action")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getAcknowledgementOfService)
                .readonlyNoSummary(AcknowledgementOfService::getConfirmReadPetition)
                .readonlyNoSummary(AcknowledgementOfService::getJurisdictionAgree)
                .readonlyNoSummary(AcknowledgementOfService::getHowToRespondApplication)
                .readonlyNoSummary(AcknowledgementOfService::getReasonCourtsOfEnglandAndWalesHaveNoJurisdiction, "jurisdictionAgree=\"No\"")
                .readonlyNoSummary(AcknowledgementOfService::getInWhichCountryIsYourLifeMainlyBased, "jurisdictionAgree=\"No\"")
            .done()
            .complex(CaseData::getApplicant2)
                .readonlyNoSummary(Applicant::getLegalProceedings)
                .readonlyNoSummary(Applicant::getLegalProceedingsDetails, "applicant2LegalProceedings=\"Yes\"")
            .done();
    }
}
