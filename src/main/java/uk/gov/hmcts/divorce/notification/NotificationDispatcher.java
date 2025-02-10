package uk.gov.hmcts.divorce.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.OTHER;

@Slf4j
@Service
public class NotificationDispatcher {

    public void send(final ApplicantNotification applicantNotification, final CaseData caseData, final Long caseId) {
        triggerNotification(applicantNotification, caseData, caseId, caseData.getApplicant1());
        triggerNotification(applicantNotification, caseData, caseId, caseData.getApplicant2());
    }

    public void send(final ApplicantNotification applicantNotification, final CaseDetails<CaseData, State> caseDetails) {
        triggerNotification(applicantNotification, caseDetails, caseDetails.getData().getApplicant1());
        triggerNotification(applicantNotification, caseDetails, caseDetails.getData().getApplicant2());
    }

    // Need different logic for NOC notification as sending to relevant applicant and their solicitor and old solicitor
    public void sendNOC(final ApplicantNotification applicantNotification,
                        final CaseData caseData, final CaseData previousCaseData, final Long caseId,
                        boolean isApplicant1, NoticeType noticeType) {
        if (noticeType == NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG) {
            sendRepresentationGrantedNotifications(isApplicant1, caseData, caseId, applicantNotification);
        }

        boolean representationRemoved = (noticeType == NoticeType.ORG_REMOVED)
            || (noticeType == NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG && applicantRepresentedBefore(isApplicant1, previousCaseData));

        if (representationRemoved) {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1OldSolicitor(previousCaseData, caseId);
            } else {
                applicantNotification.sendToApplicant2OldSolicitor(previousCaseData, caseId);
            }
        }
    }

    private void sendRepresentationGrantedNotifications(boolean isApplicant1, CaseData caseData,
                                                        long caseId, ApplicantNotification applicantNotification) {
        if (isApplicant1) {
            applicantNotification.sendToApplicant1Offline(caseData, caseId);

            if (StringUtils.isNotEmpty(caseData.getApplicant1().getEmail())) {
                applicantNotification.sendToApplicant1(caseData, caseId);
            }

            applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant2Offline(caseData, caseId);

            if (StringUtils.isNotEmpty(caseData.getApplicant2().getEmail())) {
                applicantNotification.sendToApplicant2(caseData, caseId);
            }

            applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
        }
    }

    private boolean applicantRepresentedBefore(final boolean isApplicant1, final CaseData previousCaseData) {
        return (isApplicant1 && previousCaseData.getApplicant1().isRepresented())
            || (!isApplicant1 && previousCaseData.getApplicant2().isRepresented());
    }

    private void triggerNotification(final ApplicantNotification applicantNotification,
                                     final CaseDetails<CaseData, State> caseDetails,
                                     final Applicant applicant) {
        boolean isApplicant1 = applicant.equals(caseDetails.getData().getApplicant1());
        if (applicant.isRepresented() && !applicant.isApplicantOffline()) {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1Solicitor(caseDetails);
            } else {
                applicantNotification.sendToApplicant2Solicitor(caseDetails);
            }
        } else if (applicant.isApplicantOffline() || (!isApplicant1 && isBlank(caseDetails.getData().getApplicant2EmailAddress()))) {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1Offline(caseDetails);
            } else {
                applicantNotification.sendToApplicant2Offline(caseDetails);
            }
        } else {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1(caseDetails);
            } else {
                applicantNotification.sendToApplicant2(caseDetails);
            }
        }
    }

    private void triggerNotification(final ApplicantNotification applicantNotification,
                                     final CaseData caseData,
                                     final Long caseId,
                                     final Applicant applicant) {
        boolean isApplicant1 = applicant.equals(caseData.getApplicant1());
        if (applicant.isRepresented() && !applicant.isApplicantOffline()) {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
            }
        } else if (applicant.isApplicantOffline() || (!isApplicant1 && isBlank(caseData.getApplicant2EmailAddress()))) {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1Offline(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant2Offline(caseData, caseId);
            }
        } else {
            if (isApplicant1) {
                applicantNotification.sendToApplicant1(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant2(caseData, caseId);
            }
        }
    }

    public void sendRequestForInformationNotification(ApplicantNotification applicantNotification, CaseData caseData, Long caseId)
        throws NotificationTemplateException {

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();
        if (APPLICANT.equals(requestForInformation.getRequestForInformationSoleParties())
            || APPLICANT1.equals(requestForInformation.getRequestForInformationJointParties())) {
            requestForInformationSendToApplicant1(applicantNotification, caseData, caseId);
        } else if (APPLICANT2.equals(requestForInformation.getRequestForInformationJointParties())) {
            requestForInformationSendToApplicant2(applicantNotification, caseData, caseId);
        } else if (BOTH.equals(requestForInformation.getRequestForInformationJointParties())) {
            requestForInformationSendToApplicant1(applicantNotification, caseData, caseId);
            requestForInformationSendToApplicant2(applicantNotification, caseData, caseId);
        } else if (OTHER.equals(requestForInformation.getRequestForInformationSoleParties())
            || RequestForInformationJointParties.OTHER.equals(requestForInformation.getRequestForInformationJointParties())) {
            applicantNotification.sendToOtherRecipient(caseData, caseId);
        } else {
            throw new NotificationTemplateException(
                "Unable to send Request For Information Notification for Case Id " + caseId + ". RequestForInformation parties not set.");
        }
    }

    public void sendRequestForInformationResponseNotification(ApplicantNotification applicantNotification, CaseData caseData, Long caseId)
        throws NotificationTemplateException {

        RequestForInformationResponseParties requestForInformationResponseParties =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse().getRequestForInformationResponseParties();

        if (RequestForInformationResponseParties.APPLICANT1.equals(requestForInformationResponseParties)
            || RequestForInformationResponseParties.APPLICANT1SOLICITOR.equals(requestForInformationResponseParties)) {
            requestForInformationSendToApplicant1(applicantNotification, caseData, caseId);
        } else if (RequestForInformationResponseParties.APPLICANT2.equals(requestForInformationResponseParties)
            || RequestForInformationResponseParties.APPLICANT2SOLICITOR.equals(requestForInformationResponseParties)) {
            requestForInformationSendToApplicant2(applicantNotification, caseData, caseId);
        } else {
            throw new NotificationTemplateException(
                "Unable to send Request For Information Response Notification for Case Id "
                + caseId
                + ". RequestForInformationResponse parties not set."
            );
        }
    }

    public void sendRequestForInformationResponsePartnerNotification(
                                                                    ApplicantNotification applicantNotification,
                                                                    CaseData caseData,
                                                                    Long caseId
    ) throws NotificationTemplateException {

        RequestForInformationResponseParties requestForInformationResponseParties =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse().getRequestForInformationResponseParties();

        if (RequestForInformationResponseParties.APPLICANT1.equals(requestForInformationResponseParties)
            || RequestForInformationResponseParties.APPLICANT1SOLICITOR.equals(requestForInformationResponseParties)) {
            requestForInformationSendToApplicant2(applicantNotification, caseData, caseId);
        } else if (RequestForInformationResponseParties.APPLICANT2.equals(requestForInformationResponseParties)
            || RequestForInformationResponseParties.APPLICANT2SOLICITOR.equals(requestForInformationResponseParties)) {
            requestForInformationSendToApplicant1(applicantNotification, caseData, caseId);
        } else {
            throw new NotificationTemplateException(
                "Unable to send Request For Information Response Partner Notification for Case Id "
                    + caseId
                    + ". RequestForInformationResponse parties not set."
            );
        }
    }

    private void requestForInformationSendToApplicant1(ApplicantNotification applicantNotification, CaseData caseData, Long caseId) {
        if (caseData.getApplicant1().isRepresented()) {
            if (caseData.getApplicant1().isApplicantOffline()) {
                applicantNotification.sendToApplicant1SolicitorOffline(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
            }
        } else {
            if (caseData.getApplicant1().isApplicantOffline()) {
                applicantNotification.sendToApplicant1Offline(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant1(caseData, caseId);
            }
        }
    }

    private void requestForInformationSendToApplicant2(ApplicantNotification applicantNotification, CaseData caseData, Long caseId) {
        if (caseData.getApplicant2().isRepresented()) {
            if (caseData.getApplicant2().isApplicantOffline()) {
                applicantNotification.sendToApplicant2SolicitorOffline(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
            }
        } else {
            if (caseData.getApplicant2().isApplicantOffline()) {
                applicantNotification.sendToApplicant2Offline(caseData, caseId);
            } else {
                applicantNotification.sendToApplicant2(caseData, caseId);
            }
        }
    }
}
