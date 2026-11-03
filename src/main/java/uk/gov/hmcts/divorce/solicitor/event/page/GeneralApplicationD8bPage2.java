package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

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
        pageBuilder.page("SolGenAppD8bEvidence")
            .label("solGenAppD8bEvidenceLabel", DOCUMENT_UPLOAD_LABEL_CONTENT)
            .complex(CaseData::getGeneralApplication)
            .optional(GeneralApplication::getSolGeneralApplicationD8bDocuments)
            .done();
    }
}
