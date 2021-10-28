package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;

@Component
public class SendApplicationIssueNotifications implements CaseTask {

    @Autowired
    private ApplicationIssuedNotification applicationIssuedNotification;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        if (!caseDetails.getData().getApplication().isSolicitorApplication()) {

            final CaseData caseData = caseDetails.getData();
            final Long caseId = caseDetails.getId();

            if (caseData.getApplicationType().isSole()) {
                applicationIssuedNotification.sendToSoleApplicant1(caseData, caseId);
                if (Objects.nonNull(caseData.getCaseInvite().getApplicant2InviteEmailAddress())) {
                    applicationIssuedNotification.sendToSoleRespondent(caseData, caseId);
                }
                if (caseDetails.getState() == AwaitingAos) {
                    applicationIssuedNotification.notifyApplicantOfServiceToOverseasRespondent(caseData, caseId);
                }
            } else {
                applicationIssuedNotification.sendToJointApplicant1(caseData, caseId);
                applicationIssuedNotification.sendToJointApplicant2(caseData, caseId);
            }
        }

        return caseDetails;
    }
}
