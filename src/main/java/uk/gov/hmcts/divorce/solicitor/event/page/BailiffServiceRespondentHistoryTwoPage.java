package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceRespondentHistoryTwoPage implements CcdPageConfiguration {

    private static final String RESPONDENT_POLICE_INVOLVEMENT_LABEL =
        "Has there been any police involvement with the respondent or other people living at the property?";

    private static final String RESPONDENT_POLICE_INVOLVEMENT = "applicant1BailiffHavePoliceBeenInvolved = \"Yes\"";

    private static final String RESPONDENT_PROVIDE_DETAILS_LABEL = "Provide details of any incidents";

    private static final String RESPONDENT_SOCIAL_SERVICES_INVOLVEMENT_LABEL =
        "Has there been any social services involvement with the respondent or other people living at the property?";

    private static final String RESPONDENT_SOCIAL_SERVICES_INVOLVEMENT = "applicant1BailiffHaveSocialServicesBeenInvolved = \"Yes\"";

    private static final String RESPONDENT_DANGEROUS_ANIMALS_LABEL =
        "Are any dogs or other potentially dangerous animals kept at the property?";

    private static final String RESPONDENT_DANGEROUS_ANIMALS = "applicant1BailiffAreThereDangerousAnimals = \"Yes\"";

    private static final String RESPONDENT_DANGEROUS_ANIMALS_DETAILS_LABEL = "Provide details of these animals";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsHistoryTwoPage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffHavePoliceBeenInvolved,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_POLICE_INVOLVEMENT_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPoliceInvolvedDetails,
                            RESPONDENT_POLICE_INVOLVEMENT,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_DETAILS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffHaveSocialServicesBeenInvolved,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_SOCIAL_SERVICES_INVOLVEMENT_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffSocialServicesInvolvedDetails,
                            RESPONDENT_SOCIAL_SERVICES_INVOLVEMENT,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_DETAILS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffAreThereDangerousAnimals,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_DANGEROUS_ANIMALS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffDangerousAnimalsDetails,
                            RESPONDENT_DANGEROUS_ANIMALS,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_DANGEROUS_ANIMALS_DETAILS_LABEL
                        )
                    .done()
                .done()
            .done()
            .done();
    }
}
