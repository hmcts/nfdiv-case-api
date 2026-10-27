package uk.gov.hmcts.divorce.solicitor.event.page.searchgov;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchGovAnyOtherAddressesPage implements CcdPageConfiguration {

    private static final String LABEL_ENTER_ADDITIONAL_ADDRESS = """
        ### Enter any other addresses related to the respondent ###

        Please provide any other addresses where the respondent has lived, including all addresses you are aware of.
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("searchGovPartnerOtherAddress");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getSearchGovRecordsJourneyOptions)
            .mandatory(SearchGovRecordsJourneyOptions::getKnowPartnerAdditionalAddresses)
            .label("searchGovOtherAddressesLabel1", LABEL_ENTER_ADDITIONAL_ADDRESS,
                "applicant1SearchGovRecordsKnowPartnerAdditionalAddresses=\"Yes\"")
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerAdditionalAddress1,
                "applicant1SearchGovRecordsKnowPartnerAdditionalAddresses=\"Yes\"")
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerAdditionalAddressDates1,
                "applicant1SearchGovRecordsKnowPartnerAdditionalAddresses=\"Yes\"")
            .optional(SearchGovRecordsJourneyOptions::getPartnerAdditionalAddress2,
                "applicant1SearchGovRecordsKnowPartnerAdditionalAddresses=\"Yes\"")
            .optional(SearchGovRecordsJourneyOptions::getPartnerAdditionalAddressDates2,
                "applicant1SearchGovRecordsKnowPartnerAdditionalAddresses=\"Yes\"")
            .done()
            .done()
            .done();
    }
}


