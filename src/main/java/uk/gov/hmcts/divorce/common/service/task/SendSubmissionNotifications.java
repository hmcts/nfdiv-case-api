package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.EnumSet;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
@Slf4j
public class SendSubmissionNotifications implements CaseTask {

    @Autowired
    private ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    @Autowired
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();
        final State state = caseDetails.getState();

        EnumSet<State> submittedStates = EnumSet.of(Submitted, AwaitingHWFDecision);

        if ((submittedStates.contains(state) || submittedStates.contains(caseData.getApplication().getWelshPreviousState()))
            && isEmpty(caseData.getApplication().getMissingDocumentTypes())) {
            log.info("Sending application submitted notifications for case : {}", caseId);
            notificationDispatcher.send(applicationSubmittedNotification, caseData, caseId);
        }

        log.info("Sending outstanding action notification if awaiting documents for case : {}", caseId);
        notificationDispatcher.send(applicationOutstandingActionNotification, caseData, caseId);

        return caseDetails;
    }
}
