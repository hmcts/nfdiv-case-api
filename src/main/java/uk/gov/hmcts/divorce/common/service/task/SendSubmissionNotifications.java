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
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
@Slf4j
public class SendSubmissionNotifications implements CaseTask {

    @Autowired
    private ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    @Autowired
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @Autowired
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();
        final State state = caseDetails.getState();

        if (caseData.getApplication().isSolicitorApplication()) {
            solicitorSubmittedNotification.send(caseData, caseId);
        } else {
            sendCitizenNotifications(caseData, caseId, state);
        }

        return caseDetails;
    }

    private void sendCitizenNotifications(final CaseData caseData, final Long caseId, final State state) {

        if (Submitted.equals(state) || AwaitingDocuments.equals(state)) {
            applicationSubmittedNotification.send(caseData, caseId);
        }

        if (AwaitingDocuments.equals(state)) {
            applicationOutstandingActionNotification.send(caseData, caseId);
        }
    }
}
