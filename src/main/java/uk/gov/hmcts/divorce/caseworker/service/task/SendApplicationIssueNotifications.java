package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationIssuedNotification;
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
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        if (!caseDetails.getData().getApplication().isSolicitorApplication()) {

            final CaseData caseData = caseDetails.getData();
            final Long caseId = caseDetails.getId();

            notificationDispatcher.send(applicationIssuedNotification, caseData, caseId);

            if (caseData.getApplicationType().isSole()
                && caseDetails.getState() == AwaitingAos
                && caseData.getApplicant2().isBasedOverseas()) {
                applicationIssuedNotification.notifyApplicantOfServiceToOverseasRespondent(caseData, caseId);
            }
        }

        return caseDetails;
    }
}
