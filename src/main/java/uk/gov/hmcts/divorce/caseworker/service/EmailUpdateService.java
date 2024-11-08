package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Service
@Slf4j
public class EmailUpdateService {

    @Autowired
    InviteApplicantToCaseNotification inviteApplicantToCaseNotification;
    @Autowired
    EmailUpdatedNotification emailUpdatedNotification;
    @Autowired
    NotificationDispatcher notificationDispatcher;

    public CaseDetails<CaseData, State> processUpdateForApplicant1(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData data = caseDetails.getData();

        CaseInviteApp1 invite = CaseInviteApp1.builder()
            .applicant1InviteEmailAddress(data.getApplicant1().getEmail())
            .build()
            .generateAccessCode();
        data.setCaseInviteApp1(invite);

        triggerNotificationToApplicant1(caseDetails);

        return caseDetails;
    }

    public CaseDetails<CaseData, State> processUpdateForApplicant2(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData data = caseDetails.getData();

        CaseInvite invite = CaseInvite.builder()
            .applicant2InviteEmailAddress(data.getApplicant2().getEmail())
            .build()
            .generateAccessCode();
        data.setCaseInvite(invite);

        triggerNotificationToApplicant2(caseDetails);

        return caseDetails;
    }

    public void triggerNotificationToApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        inviteApplicantToCaseNotification.send(caseData, caseId, true);
    }

    public void triggerNotificationToApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        inviteApplicantToCaseNotification.send(caseData, caseId, false);
    }

    public void sendNotificationToOldEmail(final CaseDetails<CaseData, State> caseDetails,
                                           String newEmail, boolean isApplicant1) {
        emailUpdatedNotification.send(caseDetails.getData(), caseDetails.getId(), newEmail, isApplicant1);
    }
}
