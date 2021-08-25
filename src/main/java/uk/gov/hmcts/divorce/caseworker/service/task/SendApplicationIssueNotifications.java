package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationIssueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
public class SendApplicationIssueNotifications implements CaseTask {

    @Autowired
    private JointApplicationIssueNotification jointApplicationIssueNotification;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        jointApplicationIssueNotification.sendToApplicant1(caseData, caseId);
        jointApplicationIssueNotification.sendToApplicant2(caseData, caseId);

        return caseDetails;
    }
}
