package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceExistingDecree implements CcdPageConfiguration {

    private static final String LABEL_CHECK_EXISTING_DECREE = """
        ### Check for an existing decree absolute or final order ###

        If the applicant has not heard from the respondent for more than 2 years you may need to check if they are already divorced.

        You can apply online to the Central Family Court to <a href="https://www.gov.uk/copy-decree-absolute-final-order/do-not-know-which-court"
            target="_blank" rel="noopener noreferrer">search for a divorce decree absolute or a final order (opens in a new tab)</a>.

        You'll need to search from the date that the applicant last heard from them.

        If decree absolute or final order is found, they are already divorced and you do not need to continue this application.

        If the court cannot find a decree absolute or a final order, you'll get a 'no trace' certificate which you cannot upload as
        evidence to progress the application.
        """;
    private static final String NO_TRACE_LABEL = "You will need to upload the no trace certificate at the end of this application.";
    private static final String NEVER_SHOW = "applicant1DispenseAwarePartnerLived =\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, "applicant1DispensePartnerLastSeenOver2YearsAgo=\"Yes\"");
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceExistingDecree");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .readonlyNoSummary(DispenseWithServiceJourneyOptions::getDispensePartnerLastSeenOver2YearsAgo, NEVER_SHOW)
                        .label("labelCheckExistingDecree", LABEL_CHECK_EXISTING_DECREE)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseHaveSearchedFinalOrder)
                        .label("labelWillNeedToUploadNoTraceCertificate", NO_TRACE_LABEL,
                            "applicant1DispenseHaveSearchedFinalOrder=\"Yes\"")
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseWhyNoFinalOrderSearch,
                            "applicant1DispenseHaveSearchedFinalOrder=\"No\"")
                    .done()
                .done()
            .done();
    }
}
