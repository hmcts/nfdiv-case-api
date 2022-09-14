package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
@Slf4j
public class SendFinalOrderGrantedNotifications implements CaseTask {

    @Autowired
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Sending final order granted notifications for case id: {} ", caseId);

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, caseId);

        return caseDetails;
    }
}
