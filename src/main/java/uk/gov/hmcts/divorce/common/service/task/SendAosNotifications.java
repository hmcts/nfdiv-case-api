package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
@RequiredArgsConstructor
public class SendAosNotifications implements CaseTask {

    private final SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    private final SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        if (data.getAcknowledgementOfService().isDisputed()) {
            notificationDispatcher.send(soleApplicationDisputedNotification, details);
        } else {
            notificationDispatcher.send(soleApplicationNotDisputedNotification, details);
        }

        return details;
    }
}

