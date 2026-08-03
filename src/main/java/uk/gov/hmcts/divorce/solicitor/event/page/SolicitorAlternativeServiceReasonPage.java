package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SolicitorAlternativeServiceReasonPage implements CcdPageConfiguration {

    public static final String ALTERNATIVE_SERVICE_HEADING = """
            ## Why are you applying for alternative service?

            Explain why you have not been able to send the papers to the respondent. Give as much detail as you can. This information may be
             considered by a judge as part of your application.
            """;

    public static final String SEND_PAPERS_LABEL = """
        ## Sending the papers to the respondent

        If you can show that the respondent has an email address that they actively use, the court may be able to
         email the papers to that address. You can let the court know if you want to serve the papers by email
         yourself.

        The court can only send the papers by email. If you do not have an email address for the respondent or
         would prefer to send them in a different way (for example text, WhatsApp or social media), you can do so
         but you will need to arrange this yourself. In these cases, we will email the papers to you so you can do so.


        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("alternativeServiceReason")
            .pageLabel("Alternative Service App");
        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.label("alternativeServiceLabel", ALTERNATIVE_SERVICE_HEADING)
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getAlternativeServiceJourneyOptions)
            .mandatory(AlternativeServiceJourneyOptions::getAltServiceReasonForApplying)
            .label("sendingPapersPara1", SEND_PAPERS_LABEL)
           .mandatory(AlternativeServiceJourneyOptions::getAltServiceMethod)
            .done();
    }
}
