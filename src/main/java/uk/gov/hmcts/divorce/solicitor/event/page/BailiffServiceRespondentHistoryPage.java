package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class BailiffServiceRespondentHistoryPage implements CcdPageConfiguration {

    private static final String RESPONDENTS_HISTORY_LABEL = """
        ## The respondent's history
        We will now ask you a few questions about the respondent’s history. This is to help the bailiff
        decide whether it is safe to deliver the papers to them.
        """;

    private static final String RESPONDENT_VIOLENT_LABEL = "Has the respondent ever been violent or been convicted of a violent offence?";

    private static final String RESPONDENT_VIOLENT = "applicant1BailiffHasPartnerBeenViolent = \"Yes\"";

    private static final String RESPONDENT_PROVIDE_DETAILS_LABEL = "Provide details of any incidents";

    private static final String RESPONDENT_THREATS_LABEL =
        "Has the respondent ever made verbal or written threats, either generally or specifically in relation to the "
        + "${labelContentDivorceOrCivilPartnershipApplication}?";

    private static final String RESPONDENT_THREATS = "applicant1BailiffHasPartnerMadeThreats = \"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsHistoryPage")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnershipApplication, NEVER_SHOW)
            .done()
            .label("respondentsHistoryLabel", RESPONDENTS_HISTORY_LABEL)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffHasPartnerBeenViolent,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_VIOLENT_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnerViolenceDetails,
                            RESPONDENT_VIOLENT,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_DETAILS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffHasPartnerMadeThreats,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_THREATS_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnerThreatsDetails,
                            RESPONDENT_THREATS,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_PROVIDE_DETAILS_LABEL
                        )
                    .done()
                .done()
            .done()
            .done();
    }
}
