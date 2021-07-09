package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

@Component
public class SolicitorSubmitNotification implements CaseDataUpdater {

    @Autowired
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final var caseData = caseDataContext.getCaseData();
        final var caseId = caseDataContext.getCaseId();

        solicitorSubmittedNotification.send(caseData, caseId);

        return caseDataUpdaterChain.processNext(caseDataContext);
    }
}
