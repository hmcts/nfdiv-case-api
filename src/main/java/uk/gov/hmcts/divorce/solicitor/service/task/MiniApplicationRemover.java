package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.solicitor.service.DraftApplicationRemovalService;

@Component
@Slf4j
public class MiniApplicationRemover implements CaseTask {

    @Autowired
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final var caseId = caseDetails.getId();
        final var caseData = caseDetails.getData();

        log.info("Removing application documents from case data and document management for {}", caseId);

        final var documentsExcludingApplication =
            draftApplicationRemovalService.removeDraftApplicationDocument(
                caseData.getDocumentsGenerated(),
                caseId
            );

        caseData.setDocumentsGenerated(documentsExcludingApplication);

        log.info("Successfully removed application documents from case data for case id {}", caseId);

        return caseDetails;
    }
}
