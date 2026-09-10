package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceRespondentHistoryThreePage implements CcdPageConfiguration {

    private static final String RESPONDENT_MENTAL_HEALTH_LABEL =
        "Is the respondent known to have any mental health issues or known to use/abuse drugs or alcohol in any way that may affect "
        + "their behaviour?";

    private static final String RESPONDENT_MENTAL_HEALTH = "applicant1BailiffDoesPartnerHaveMentalIssues = \"Yes\"";

    private static final String RESPONDENT_PROVIDE_INFORMATION_LABEL = "Provide as much information as you can";

    private static final String RESPONDENT_FIREARMS_LABEL =
        "Does the respondent hold a firearms licence or have any firearms convictions?";

    private static final String RESPONDENT_FIREARMS = "applicant1BailiffDoesPartnerHoldFirearmsLicense = \"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsHistoryThreePage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffDoesPartnerHaveMentalIssues,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_MENTAL_HEALTH_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnerMentalIssuesDetails,
                            RESPONDENT_MENTAL_HEALTH,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_INFORMATION_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffDoesPartnerHoldFirearmsLicense,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_FIREARMS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnerFirearmsLicenseDetails,
                            RESPONDENT_FIREARMS,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_INFORMATION_LABEL
                        )
                    .done()
                .done()
            .done()
            .done();
    }
}
