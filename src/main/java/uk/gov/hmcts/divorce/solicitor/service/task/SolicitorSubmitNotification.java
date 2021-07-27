package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

@Component
public class SolicitorSubmitNotification implements CaseTask {

    @Autowired
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final var caseData = caseDetails.getData();
        final var caseId = caseDetails.getId();

        solicitorSubmittedNotification.send(caseData, caseId);

        return caseDetails;
    }
}
