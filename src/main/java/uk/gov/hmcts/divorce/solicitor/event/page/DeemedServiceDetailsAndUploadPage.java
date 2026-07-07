package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class DeemedServiceDetailsAndUploadPage implements CcdPageConfiguration {

    private static final String DEEMED_EVIDENCE_LABEL = "## Tell us about your evidence \n\n"
        + "Give as much detail as you can. The judge needs to be satisfied "
        + "that the respondent has received the papers before they can grant the application. If the evidence does not show "
        + "the date of the conversations, you will need to explain this. You will be able to upload all the evidence "
        + "you have described.";

    private static final String DEEMED_STATEMENT_LABEL = "## Provide a statement (Optional) \n\n"
        + "You can provide a statement explaining how you know the respondent has received the divorce papers.";

    private static final String DEEMED_EVIDENCE_DOCS_LABEL = "## Upload your evidence \n\n "
        + "Upload your evidence to support your application for deemed service. This should be evidence you've found from after "
        + "the date that the application was issued: ${issueDate}.\n\nIf you're uploading images or "
        + "screenshots of a recent conversation between the applicant and the respondent by text, email or social media, make sure "
        + "they include:\n\n"
        + "- the respondent's name\n"
        + "- the date the messages were sent\n"
        + "- the respondent's email address, phone number or social media username as appropriate\n\n"
        + "If your evidence is a conversation in a language other than English, you'll need to provide a "
        + "<a href=\"https://www.gov.uk/certifying-a-document#certifying-a-translation\" target=\"_blank\">certified translation</a>\n\n"
        + "## Applicant 1 uploaded documents";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("deemedServiceDetailsAndUpload")
            .pageLabel("Deemed Service App")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDeemedServiceJourneyOptions)
                        .label("deemedEvidenceLabel", DEEMED_EVIDENCE_LABEL)
                        .mandatory(DeemedServiceJourneyOptions::getDeemedEvidenceDetails)
                        .label("deemedStatementLabel", DEEMED_STATEMENT_LABEL)
                        .optionalNoSummary(DeemedServiceJourneyOptions::getDeemedNoEvidenceStatement)
                        .label("deemedEvidenceDocsLabel", DEEMED_EVIDENCE_DOCS_LABEL)
                        .optional(DeemedServiceJourneyOptions::getDeemedEvidenceDocs)
                    .done()
                .done()
            .done();
    }
}
