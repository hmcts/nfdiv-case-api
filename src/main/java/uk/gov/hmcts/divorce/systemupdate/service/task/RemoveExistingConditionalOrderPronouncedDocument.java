package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@Slf4j
public class RemoveExistingConditionalOrderPronouncedDocument implements CaseTask {

    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Removing Conditional Order granted pdf for CaseID: {}", caseDetails.getId());

        caseDetails.getData().getDocuments().removeDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED);

        return caseDetails;
    }
}
