package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
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
                .readonlyNoSummary(LabelContent::getTheApplicant2UC, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .mandatoryWithLabel(MarriageDetails::getDate,
                        "Date of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getPlaceOfMarriage,
                        "Place of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getCountryOfMarriage,
                        "Country of ${labelContentMarriageOrCivilPartnership}")
                    .optional(MarriageDetails::getApplicant1Name)
                    .optional(MarriageDetails::getApplicant2Name)
                .done()
            .done();
    }
}
