package uk.gov.hmcts.divorce.solicitor.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.solicitor.service.DraftApplicationRemovalService;

@Component
@Slf4j
public class MiniApplicationRemover implements CaseDataUpdater {

    @Autowired
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final var caseId = caseDataContext.getCaseId();
        final var updatedCaseData = caseDataContext.copyOfCaseData();

        log.info("Removing application documents from case data and document management for {}", caseId);

        final var documentsExcludingApplication =
            draftApplicationRemovalService.removeDraftApplicationDocument(
                caseDataContext.getCaseData().getDocumentsGenerated(),
                caseId,
                caseDataContext.getUserAuthToken()
            );

        updatedCaseData.setDocumentsGenerated(documentsExcludingApplication);

        log.info("Successfully removed application documents from case data for case id {}", caseId);

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
