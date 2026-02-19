package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.FinancialOrderRequestedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
@RequiredArgsConstructor
public class SendFinancialOrderRequestedNotifications implements CaseTask {

    private final FinancialOrderRequestedNotification financialOrderRequestedNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        if (YesOrNo.YES.equals(caseData.getApplicant1().getFinancialOrder())
            || YesOrNo.YES.equals(caseData.getApplicant2().getFinancialOrder())) {
            notificationDispatcher.send(financialOrderRequestedNotification, caseData, caseId);
        }
        return caseDetails;
    }
}
