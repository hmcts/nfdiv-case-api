package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SolicitorAlternativeServiceMethodPage implements CcdPageConfiguration {

    private static final String ALT_SERVICE_EMAIL_SHOW_CONDITION =
        "applicant1AltServiceMethod = \"byEmail\" OR applicant1AltServiceMethod = \"emailAndDifferentWay\" " ;
    public static final String PAPER_SEND_METHOD_HEADING = "## How would you like the papers to be sent by email?";

    public static final String SEND_PAPERS_METHOD_SOLICITOR_HINT = "If you select Solicitor Service, you will need to provide a reason";

    public static final String CHOOSE_SEND_PAPERS_METHOD = "## Choose how you want to send the papers to the respondent";

    public static final String CONFIRM_RESPONDENT_METHOD_EVIDENCE =
        "You will need to provide evidence that the respondent actively uses each method you choose.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("alternativeServiceMethod")
            .pageLabel("Alternative Service App");
        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.label("alternativeServiceMethodLabel", PAPER_SEND_METHOD_HEADING)
            .label("sendPapersSolicitorHintLabel", SEND_PAPERS_METHOD_SOLICITOR_HINT)
            .complex(CaseData::getAlternativeService)
            .mandatory(AlternativeService::getSolAlternativeServiceReason)
            .done()
            .complex(CaseData::getApplication)
            .mandatory(Application::getServiceMethod)
            .done()
            .complex(CaseData::getAlternativeService)
            .mandatory(AlternativeService::getSolicitorServiceReason, "serviceMethod = \"solicitorService\"")
            .done()
            .complex(CaseData::getApplicant2)
            .mandatoryWithoutDefaultValue(Applicant::getNonConfidentialEmail, ALT_SERVICE_EMAIL_SHOW_CONDITION,"Respondent's email address")
            .done()
            .label("choosePaperSendMethod", CHOOSE_SEND_PAPERS_METHOD)
            .label("confirmRespondentMethodEvidence", CONFIRM_RESPONDENT_METHOD_EVIDENCE)

            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getAlternativeServiceJourneyOptions)
                        .mandatoryWithLabel(AlternativeServiceJourneyOptions::getAltServiceDifferentWays,
                            "Select all that apply")
                        .mandatory(AlternativeServiceJourneyOptions::getSolicitorContactMethodOtherDetails,
                            "applicant1AltServiceDifferentWaysCONTAINS\"other\"",
                NO_DEFAULT_VALUE)
                    .done();
    }
}
