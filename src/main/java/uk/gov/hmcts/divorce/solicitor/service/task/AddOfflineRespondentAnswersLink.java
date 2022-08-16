package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

@Component
public class AddOfflineRespondentAnswersLink implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        caseData.getDocuments().getFirstUploadedDocumentLinkWith(RESPONDENT_ANSWERS)
            .ifPresent(document -> caseData.getConditionalOrder().setRespondentAnswersLink(document));

        return caseDetails;
    }
}
