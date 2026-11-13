package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.D8bAnswersJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD8bPage2 implements CcdPageConfiguration {

    private static final String UPLOAD_LABEL = "Respondent uploaded documents";

    public static final String DOCUMENT_UPLOAD_LABEL_CONTENT = """
        ## Upload your documents ##

        Upload the Form D8b Answers and any supporting evidence.

        The court cannot accept video or audio recordings as evidence.

        The file must be in jpeg, tiff, png or pdf format.

        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD8bEvidence");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }

        page.label("solGenAppD8bEvidenceLabel", DOCUMENT_UPLOAD_LABEL_CONTENT)
            .complex(CaseData::getApplicant2)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getD8BAnswerJourneyOptions)
            .optional(D8bAnswersJourneyOptions::getSolD8bDocuments)
            .done();
    }
}
