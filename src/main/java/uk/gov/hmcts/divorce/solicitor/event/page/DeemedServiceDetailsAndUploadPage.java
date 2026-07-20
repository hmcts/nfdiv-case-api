package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class DeemedServiceDetailsAndUploadPage implements CcdPageConfiguration {

    private final String pageShowCondition;

    private static final String DEEMED_EVIDENCE_LABEL = """
            ## Tell us about your evidence

            Give as much detail as you can. The judge needs to be satisfied that the respondent has received the papers before they can
            grant the application. If the evidence does not show the date of the conversations, you will need to explain this. You will be
            able to upload all the evidence you have described.
            """;

    private static final String DEEMED_STATEMENT_LABEL = """
            ## Provide a statement (Optional)

            You can provide a statement explaining how you know the respondent has received the divorce papers.
            """;

    private static final String DEEMED_EVIDENCE_DOCS_LABEL = """
            ## Upload your evidence

            Upload your evidence to support your application for deemed service. This should be evidence you've found from after the date
            that the application was issued: ${issueDate}.

            If you're uploading images or screenshots of a recent conversation between the applicant and the respondent by text, email or
            social media, make sure they include:

            - the respondent's name
            - the date the messages were sent
            - the respondent's email address, phone number or social media username as appropriate

            If your evidence is a conversation in a language other than English, you'll need to provide a
            <a href="https://www.gov.uk/certifying-a-document#certifying-a-translation" target="_blank">certified translation</a>

            ## Applicant 1 uploaded documents
            """;

    public DeemedServiceDetailsAndUploadPage() {
        this(null);
    }

    public DeemedServiceDetailsAndUploadPage(String pageShowCondition) {
        this.pageShowCondition = pageShowCondition;
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        var page = pageBuilder.page("deemedServiceDetailsAndUpload")
            .pageLabel("Deemed Service App");
        if (pageShowCondition != null) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDeemedServiceJourneyOptions)
                        .label("deemedEvidenceLabel", DEEMED_EVIDENCE_LABEL)
                        .mandatory(DeemedServiceJourneyOptions::getDeemedEvidenceDetails)
                        .label("deemedStatementLabel", DEEMED_STATEMENT_LABEL)
                        .optionalNoSummary(DeemedServiceJourneyOptions::getDeemedNoEvidenceStatement)
                        .label("deemedEvidenceDocsLabel", DEEMED_EVIDENCE_DOCS_LABEL)
                    .done()
                    .optionalWithLabel(InterimApplicationOptions::getInterimAppsEvidenceDocs, "Applicant 1 uploaded documents")
                .done()
            .done();
    }
}
