package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendSubmissionNotifications implements CaseTask {

    private final ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    private final ApplicationSubmittedNotification applicationSubmittedNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();
        Long caseId = caseDetails.getId();
        State state = caseDetails.getState();
        Application application = caseData.getApplication();

        EnumSet<State> submittedStates = EnumSet.of(Submitted, AwaitingHWFDecision);
        boolean hasSubmittedDocuments = !application.hasAwaitingApplicant1Documents()
            || (!caseData.getApplicationType().isSole() && !application.hasAwaitingApplicant2Documents());
        boolean isAddressProvided = caseData.getApplicationType().isSole() && !application.knowsRespondentAddress();

        if ((submittedStates.contains(state) || submittedStates.contains(application.getWelshPreviousState()))
            && hasSubmittedDocuments && isAddressProvided) {
            log.info("Sending application submitted notifications for case : {}", caseId);
            notificationDispatcher.send(applicationSubmittedNotification, caseData, caseId);
        } else {

            log.info("Sending outstanding action needed notification for case : {}", caseId);

            notificationDispatcher.send(applicationOutstandingActionNotification, caseData, caseId);
        }

        return caseDetails;
    }
}
