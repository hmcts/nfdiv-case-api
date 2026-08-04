package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class SolicitorAlternativeServiceAbleToUploadEvidence implements CcdPageConfiguration {

    private static final String CAN_UPLOAD_EVIDENCE_HINT = """
            The evidence you provide may help the court decide whether the papers can be served in the way you've requested.
             For example, this may include a photo or screenshot of a recent conversation by text or email, or a post by the respondent
              on social media.""";

    private static final String WHY_SEND_THIS_WAY = "Why are you applying to send the papers in this way?";

    private static final String WHY_SEND_THIS_WAY_HINT = """
            Tell us why you think the respondent will receive the papers in this way. If a friend or relative will be sending the papers on
             behalf of the applicant, you'll need to tell us who this is.

            You should also explain why you are not able to upload evidence.

            Explain in as much detail as you can so that the judge can consider whether to grant your application.

            """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("altServiceAbleToUploadEvidence")
            .pageLabel("Alternative Service App")
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .mandatory(InterimApplicationOptions::getInterimAppsCanUploadEvidence,
                null, NO_DEFAULT_VALUE, null, CAN_UPLOAD_EVIDENCE_HINT)
            .complex(InterimApplicationOptions::getAlternativeServiceJourneyOptions)
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceSuccessfulSendReason,
                "applicant1InterimAppsCanUploadEvidence=\"Yes\"")
            .mandatory(AlternativeServiceJourneyOptions::getAltServiceMethodJustification,
                "applicant1InterimAppsCanUploadEvidence=\"No\"",
                NO_DEFAULT_VALUE, WHY_SEND_THIS_WAY, WHY_SEND_THIS_WAY_HINT)
            .done();
    }
}
