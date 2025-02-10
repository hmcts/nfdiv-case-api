package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
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

    public CaseDetails<CaseData, State> processEmailUpdate(final CaseDetails<CaseData, State> caseDetails,
                                                      final CaseDetails<CaseData, State> beforeCaseDetails,
                                                      boolean isApplicant1) {

        final CaseData data = caseDetails.getData();

        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();

        if (applicant.getEmail() == null || applicant.getEmail().isBlank()
            || applicant.isRepresented()) {
            return caseDetails;
        }

        //Do not send invite to respondent if sole application and case hasn't been issued
        if (data.getApplicationType().isSole() && (data.getApplication().getIssueDate() == null) && !isApplicant1) {
            return  caseDetails;
        }

        createCaseInvite(data, isApplicant1);

        sendInviteToApplicantEmail(data, caseDetails.getId(), isApplicant1);

        sendNotificationToOldEmail(beforeCaseDetails, applicant.getEmail(), isApplicant1);

        return caseDetails;
    }

    public void createCaseInvite(final CaseData data, boolean isApplicant1) {
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        if (isApplicant1) {
            CaseInviteApp1 invite = CaseInviteApp1.builder()
                .applicant1InviteEmailAddress(applicant.getEmail())
                .build()
                .generateAccessCode();
            data.setCaseInviteApp1(invite);
        } else {
            CaseInvite invite = CaseInvite.builder()
                .applicant2InviteEmailAddress(applicant.getEmail())
                .build()
                .generateAccessCode();
            data.setCaseInvite(invite);
        }
    }

    public void sendInviteToApplicantEmail(final CaseData caseData, Long id, boolean isApplicant1) {
        inviteApplicantToCaseNotification.send(caseData, id, isApplicant1);
    }

    public void sendNotificationToOldEmail(final CaseDetails<CaseData, State> caseDetails,
                                           String newEmail, boolean isApplicant1) {
        emailUpdatedNotification.send(caseDetails.getData(), caseDetails.getId(), newEmail, isApplicant1);
    }
}
