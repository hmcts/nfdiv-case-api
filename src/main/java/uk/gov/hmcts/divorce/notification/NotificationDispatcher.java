package uk.gov.hmcts.divorce.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class NotificationDispatcher {

    public void send(final ApplicantNotification applicantNotification, final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant1().isRepresented() && !caseData.getApplicant1().isOffline() && !applicantNotification.alreadySentToApplicant1Solicitor) {
            applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
            applicantNotification.alreadySentToApplicant1Solicitor = true;

        } else if (caseData.getApplicant1().isOffline() && !applicantNotification.alreadySentToApplicant1Offline) {
            applicantNotification.sendToApplicant1Offline(caseData, caseId);
            applicantNotification.alreadySentToApplicant1Offline = true;

        } else if (!applicantNotification.alreadySentToApplicant1) {
            applicantNotification.sendToApplicant1(caseData, caseId);
            applicantNotification.alreadySentToApplicant1 = true;
        }

        if (caseData.getApplicant2().isRepresented() && !caseData.getApplicant2().isOffline() && !applicantNotification.alreadySentToApplicant2Solicitor) {
            applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
            applicantNotification.alreadySentToApplicant2Solicitor = true;

        } else if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isOffline() && !applicantNotification.alreadySentToApplicant2Offline) {
            applicantNotification.sendToApplicant2Offline(caseData, caseId);
            applicantNotification.alreadySentToApplicant2Offline = true;

        } else if (!applicantNotification.alreadySentToApplicant2) {
            applicantNotification.sendToApplicant2(caseData, caseId);
            applicantNotification.alreadySentToApplicant2 = true;
        }
    }
}
