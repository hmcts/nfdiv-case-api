package uk.gov.hmcts.divorce.notification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
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

    // Need different logic for NOC notification as sending to relevant applicant and their solicitor and old solicitor
    public void sendNOC(final ApplicantNotification applicantNotification,
                        final CaseData caseData, final CaseData previousCaseData, final Long caseId,
                        boolean isApplicant1, NoticeType noticeType) {
        if (noticeType == NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG || noticeType == NoticeType.NEW_DIGITAL_SOLICITOR_EXISTING_ORG) {
            if (isApplicant1) {
                if (StringUtils.isNotEmpty(caseData.getApplicant1().getEmail())) {
                    applicantNotification.sendToApplicant1(caseData, caseId);
                }
                applicantNotification.sendToApplicant1Solicitor(caseData, caseId);

            } else {
                if (StringUtils.isNotEmpty(caseData.getApplicant2().getEmail())) {
                    applicantNotification.sendToApplicant2(caseData, caseId);
                }
                applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
            }
        }
        if (isApplicant1) {
            applicantNotification.sendToApplicant1OldSolicitor(previousCaseData, caseId);
        } else {
            applicantNotification.sendToApplicant2OldSolicitor(previousCaseData, caseId);
        }
    }
}
