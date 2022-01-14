package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

public class AmendCase implements CcdPageConfiguration {
    private static final String ALWAYS_HIDE = "marriageCountryOfMarriage=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendCase")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optionalWithLabel(MarriageDetails::getDate,
                        "Date of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getPlaceOfMarriage,
                        "Place of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getCountryOfMarriage,
                        "Country of ${labelContentMarriageOrCivilPartnership}")
                .done()
            .done()
            .complex(CaseData::getApplicant1)
                .optionalWithLabel(Applicant::getFirstName,
                    "${labelContentApplicantsOrApplicant1s} first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .optionalWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
            .done()
            .complex(CaseData::getApplicant2)
                .optionalWithLabel(Applicant::getFirstName,
                    "${labelContentRespondentsOrApplicant2s} first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentRespondentsOrApplicant2s} middle name")
                .optionalWithLabel(Applicant::getLastName,
                    "${labelContentRespondentsOrApplicant2s} last name")
            .done();
    }
}
