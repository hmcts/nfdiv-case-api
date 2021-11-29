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
            .mandatory(CaseData::getD11Document)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData caseData = details.getData();
        DocumentType documentType = caseData.getD11Document().getDocumentType();

        if (!DocumentType.D11.equals(documentType)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please upload a D11 document type"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .build();
    }
}
