package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class SolicitorAlternativeServiceMethodPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private static final String SHOW_WHEN_NOT_ALT_SERVICE_DIFFERENT_WAY = "applicant1AltServiceMethod != \"inADifferentWay\"";

    public static final String CHOOSE_SEND_PAPERS_METHOD = "Choose how you want to send the papers to the respondent";

    public static final String CONFIRM_RESPONDENT_METHOD_EVIDENCE =
        "You will need to provide evidence that the respondent actively uses each method you choose.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("alternativeServiceMethod")
            .pageLabel("Alternative Service App")
            .complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getAlternativeServiceJourneyOptions)
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceMethod, SHOW_WHEN_NOT_ALT_SERVICE_DIFFERENT_WAY)
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceSolicitorServiceReason,
                "applicant1SolAltServiceMethod = \"solicitorService\"")
            .mandatoryWithoutDefaultValue(AlternativeServiceJourneyOptions::getAltServicePartnerEmail,
                SHOW_WHEN_NOT_ALT_SERVICE_DIFFERENT_WAY, "Respondent's email address")
            .label("choosePaperSendMethod", CHOOSE_SEND_PAPERS_METHOD)
            .label("confirmRespondentMethodEvidence", CONFIRM_RESPONDENT_METHOD_EVIDENCE)
            .label("selectAll", "Select all that apply")
            .mandatoryWithLabel(AlternativeServiceJourneyOptions::getAltServiceDifferentWays,
                "How do you want to send the ${labelContentDivorceOrCivilPartnership} papers")
            .mandatory(AlternativeServiceJourneyOptions::getAltServicePartnerPhone,
                "applicant1AltServiceDifferentWaysCONTAINS\"textMessage\"",
                NO_DEFAULT_VALUE)
            .mandatory(AlternativeServiceJourneyOptions::getAltServicePartnerWANum,
                "applicant1AltServiceDifferentWaysCONTAINS\"whatsapp\"",
                NO_DEFAULT_VALUE)
            .mandatory(AlternativeServiceJourneyOptions::getAltServicePartnerSocialDetails,
                "applicant1AltServiceDifferentWaysCONTAINS\"socialMedia\"",
                NO_DEFAULT_VALUE)
            .mandatory(AlternativeServiceJourneyOptions::getAltServicePartnerOtherDetails,
                "applicant1AltServiceDifferentWaysCONTAINS\"other\"",
                NO_DEFAULT_VALUE)
            .done();
    }
}
