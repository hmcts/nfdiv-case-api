package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import static java.util.Collections.singletonList;

public class AnswerReceivedUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedUploadDocument", this::midEvent)
            .pageLabel("Upload document")
            .mandatory(CaseData::getUploadD11Document)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData caseData = details.getData();
        DocumentType documentType = caseData.getUploadD11Document().getDocumentType();

        if (!DocumentType.D11.equals(documentType)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("The D11 document that was emailed to you needs to be uploaded"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .build();
    }
}
