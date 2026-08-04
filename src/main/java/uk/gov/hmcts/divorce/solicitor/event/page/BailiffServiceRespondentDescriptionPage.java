package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceRespondentDescriptionPage implements CcdPageConfiguration {

    private static final String RESPONDENTS_HEIGHT_LABEL = "How tall is the respondent?";
    private static final String RESPONDENTS_HEIGHT_HINT = "For example, 185cm or 6'1\"";

    private static final String RESPONDENTS_HAIR_LABEL = "What is the respondent's hair colour?";

    private static final String RESPONDENTS_EYE_LABEL = "What is the respondent's eye colour?";

    private static final String RESPONDENTS_ETHNIC_GROUP_LABEL = "What is the respondent's ethnic group?";
    private static final String RESPONDENTS_ETHNIC_GROUP_HINT = "For example, Bangladeshi";

    private static final String RESPONDENTS_OTHER_FEATURES_LABEL = "Does the respondent have any other distinguishing features?";
    private static final String RESPONDENTS_OTHER_FEATURES_HINT = """
        For example, tattoo of a word on left arm, or a scar on the right side of forehead. Give as much detail as possible.
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsDetailsPage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(BailiffServiceJourneyOptions::getBailiffPartnersHeight, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENTS_HEIGHT_LABEL, RESPONDENTS_HEIGHT_HINT)
                        .mandatory(BailiffServiceJourneyOptions::getBailiffPartnersHairColour, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENTS_HAIR_LABEL)
                        .mandatory(BailiffServiceJourneyOptions::getBailiffPartnersEyeColour, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENTS_EYE_LABEL)
                        .mandatory(BailiffServiceJourneyOptions::getBailiffPartnersEthnicGroup, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENTS_ETHNIC_GROUP_LABEL, RESPONDENTS_ETHNIC_GROUP_HINT)
                        .mandatory(BailiffServiceJourneyOptions::getBailiffPartnersDistinguishingFeatures, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENTS_OTHER_FEATURES_LABEL, RESPONDENTS_OTHER_FEATURES_HINT)
                    .done()
                .done()
            .done()
        .done();
    }
}
