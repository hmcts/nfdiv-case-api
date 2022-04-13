package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;

public class AnswerReceivedUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getAnswerReceivedSupportingDocuments)
                .done()
            .done();
    }
}
