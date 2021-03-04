package uk.gov.hmcts.reform.divorce.caseapi.notification.handler;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;

public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    public void notifyApplicant(){
        throw new NotImplementedException("Temporarily not implemented");
        // This method will call notification service for save and sign out notification
    }
}
