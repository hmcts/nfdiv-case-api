package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedOverseasNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;

@Component
public class SendApplicationIssueNotifications implements CaseTask {

    @Autowired
    private ApplicationIssuedNotification applicationIssuedNotification;

    @Autowired
    private ApplicationIssuedOverseasNotification applicationIssuedOverseasNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        notificationDispatcher.send(applicationIssuedNotification, caseData, caseId);

        if (caseDetails.getState() == AwaitingAos) {
            notificationDispatcher.send(applicationIssuedOverseasNotification, caseData, caseId);
        }

        return caseDetails;
    }
}
