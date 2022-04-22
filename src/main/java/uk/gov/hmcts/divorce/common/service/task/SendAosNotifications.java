package uk.gov.hmcts.divorce.common.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
public class SendAosNotifications implements CaseTask {

    @Autowired
    private SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    @Autowired
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        if (data.getAcknowledgementOfService().isDisputed()) {
            notificationDispatcher.send(soleApplicationDisputedNotification, data, details.getId());
        } else {
            notificationDispatcher.send(soleApplicationNotDisputedNotification, data, details.getId());
        }

        return details;
    }
}

