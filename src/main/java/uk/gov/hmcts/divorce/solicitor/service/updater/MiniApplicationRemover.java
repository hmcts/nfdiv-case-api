package uk.gov.hmcts.divorce.solicitor.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
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

        log.info("Removing petition documents from case data and document management for {}", caseId);

        final var documentsExcludingPetition =
            draftApplicationRemovalService.removeDraftApplicationDocument(
                caseDataContext.getCaseData().getDocumentsGenerated(),
                caseId,
                caseDataContext.getUserAuthToken()
            );

        updatedCaseData.setDocumentsGenerated(documentsExcludingPetition);

        log.info("Successfully removed petition documents from case data for case id {}", caseId);

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
