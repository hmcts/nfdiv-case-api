package uk.gov.hmcts.divorce.common.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.divorce.citizen.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationSolicitorSubmitAosNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
public class SendSolicitorAosNotifications implements CaseTask {

    @Autowired
    private SoleApplicationSolicitorSubmitAosNotification soleApplicationSolicitorSubmitAosNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        if (data.getApplicant2().isRepresented()) {
            notificationDispatcher.send(soleApplicationSolicitorSubmitAosNotification, data, details.getId());
        }

        return details;
    }
}

