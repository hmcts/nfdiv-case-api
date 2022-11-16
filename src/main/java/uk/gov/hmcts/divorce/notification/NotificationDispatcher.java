package uk.gov.hmcts.divorce.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class NotificationDispatcher {

    public void send(final ApplicantNotification applicantNotification, final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant1().isRepresented() && !caseData.getApplicant1().isApplicantOffline()) {
            applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
        } else if (caseData.getApplicant1().isApplicantOffline()) {
            applicantNotification.sendToApplicant1Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant1(caseData, caseId);
        }

        if (caseData.getApplicant2().isRepresented() && !caseData.getApplicant2().isApplicantOffline()) {
            applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
        } else if (caseData.getApplicant2().isApplicantOffline() || isBlank(caseData.getApplicant2EmailAddress())) {
            applicantNotification.sendToApplicant2Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant2(caseData, caseId);
        }
    }
}
