package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress.LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE;

public class DispenseWithServiceSearchingOnline implements CcdPageConfiguration {

    private static final String LABEL_SEARCHING_RESPONDENT_ONLINE = """
        ### Finding the respondent online by searching the internet ###

        You could consider using a search engine to look up the respondent's name or trying to find them on social media platforms.

        If you can find:
        - an up to date postal address, you could update their postal address and resend (reissue) the papers at no additional cost
        - evidence that they actively use an email address, phone number or social media account, you could apply for alternative service
        """;

    private static final String TRIED_SEARCHING_ONLINE_SHOW_CONDITION_YES = "applicant1DispenseTriedSearchingOnline=\"Yes\"";
    private static final String TRIED_SEARCHING_ONLINE_SHOW_CONDITION_NO = "applicant1DispenseTriedSearchingOnline=\"No\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceOnlineSearching")
                    .pageLabel("Dispense with service app");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("labelRespondentSearchingOnline", LABEL_SEARCHING_RESPONDENT_ONLINE)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTriedSearchingOnline)
                        .label("labelOnlineSearchResults", "### Finding the respondent online ###",
                            TRIED_SEARCHING_ONLINE_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseSearchingOnlineResults,
                            TRIED_SEARCHING_ONLINE_SHOW_CONDITION_YES)
                        .label("labelYouCanUploadSearchingOnlineEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE,
                            TRIED_SEARCHING_ONLINE_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseWhyNoSearchingOnline,
                            TRIED_SEARCHING_ONLINE_SHOW_CONDITION_NO)
                    .done()
                .done()
            .done();
    }
}
