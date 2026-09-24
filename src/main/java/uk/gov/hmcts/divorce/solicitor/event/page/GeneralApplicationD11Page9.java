package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class GeneralApplicationD11Page9 implements CcdPageConfiguration {

    private final String detailsNotCorrectCondition;

    public GeneralApplicationD11Page9(String applicantFieldPrefix) {
        this.detailsNotCorrectCondition =
            applicantFieldPrefix + "GenAppPartnerDetailsCorrect=\"No\"";
    }

    public static final String UPDATE_DETAILS_LABEL = """
        ## We need up to date information for the other party

        You can <a href="https://contact-us-about-a-divorce-application.form.service.justice.gov.uk/"
                                 target="_blank" rel="noopener noreferrer">update their details using our online form (opens in new tab)</a>.

        You could also contact them and ask them to update their details, if it’s safe to do so.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11DetailsNotCorrect")
            .showCondition(detailsNotCorrectCondition)
            .label("solGeneralAppD11UpdateLabel", UPDATE_DETAILS_LABEL);
    }
}
