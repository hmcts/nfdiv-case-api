package uk.gov.hmcts.divorce.solicitor.event.page.searchgov;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchGovRespondentDetailsPage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("searchGovPartnerDetails");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getSearchGovRecordsJourneyOptions)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerName)
            .mandatory(SearchGovRecordsJourneyOptions::getKnowPartnerDateOfBirth)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerDateOfBirth,
                "applicant1SearchGovRecordsKnowPartnerDateOfBirth=\"Yes\"")
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerApproximateAge,
                "applicant1SearchGovRecordsKnowPartnerDateOfBirth=\"No\"")
            .mandatory(SearchGovRecordsJourneyOptions::getKnowPartnerNationalInsurance)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerNationalInsurance,
                "applicant1SearchGovRecordsKnowPartnerNationalInsurance=\"Yes\"")
            .done()
            .done()
            .done();
    }
}
