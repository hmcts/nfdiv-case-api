package uk.gov.hmcts.divorce.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

@Service
public class NotificationDispatcher {

    public void send(final ApplicantNotification applicantNotification, final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
        } else if (caseData.getApplicant1().isOffline()) {
            applicantNotification.sendToApplicant1Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant1(caseData, caseId);
        }

        if (caseData.getApplicant2().isRepresented()) {
            applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
        } else if (caseData.getApplicant2().isOffline()) {
            applicantNotification.sendToApplicant2Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant2(caseData, caseId);
        }
    }
}
