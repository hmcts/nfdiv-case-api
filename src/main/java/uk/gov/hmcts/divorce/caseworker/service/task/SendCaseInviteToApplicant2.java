package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ReInviteApplicant2Notification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Component
@Slf4j
public class SendCaseInviteToApplicant2 implements CaseTask {

    @Autowired
    ReInviteApplicant2Notification reInviteApplicant2Notification;

    @Autowired
    NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        notificationDispatcher.send(reInviteApplicant2Notification, caseData, caseId);

        return details;
    }
}
