package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.FinancialOrderRequestedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
public class SendFinancialOrderRequestedNotifications implements CaseTask {

    @Autowired
    private FinancialOrderRequestedNotification financialOrderRequestedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        if ((null != caseData.getApplicant1().getFinancialOrder()
            && caseData.getApplicant1().getFinancialOrder().toBoolean())
            || (null != caseData.getApplicant2().getFinancialOrder())
            && caseData.getApplicant2().getFinancialOrder().toBoolean()) {
            notificationDispatcher.send(financialOrderRequestedNotification, caseData, caseId);
        }
        return caseDetails;
    }
}
