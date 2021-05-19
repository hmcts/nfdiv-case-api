package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;

public class MarriageCertificateDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageCertificateDetails")
            .pageLabel("Marriage certificate details")
            .label(
                "marriageCertDetailsEditMessage",
                "You can make changes at the end of your application.")
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getDate)
                .done()
            .mandatory(CaseData::getMarriageApplicant1Name)
            .mandatory(CaseData::getMarriageApplicant2Name)
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getMarriedInUk)
                .mandatory(
                    MarriageDetails::getPlaceOfMarriage,
                    "marriageMarriedInUk=\"No\""
                )
                .mandatory(
                    MarriageDetails::getCountryOfMarriage,
                    "marriageMarriedInUk=\"No\""
                )
            .done();
    }
}
