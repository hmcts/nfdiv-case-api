package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class MarriageCertificateDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageCertificateDetails")
            .pageLabel("Marriage certificate details")
            .mandatory(CaseData::getMarriageDate)
            .mandatory(CaseData::getMarriageApplicant1Name)
            .mandatory(CaseData::getMarriageApplicant2Name)
            .mandatory(CaseData::getMarriedInUk)
            .mandatory(
                CaseData::getMarriagePlaceOfMarriage,
                "marriedInUk=\"No\""
            )
            .mandatory(
                CaseData::getCountryName,
                "marriedInUk=\"No\""
            );
    }
}
