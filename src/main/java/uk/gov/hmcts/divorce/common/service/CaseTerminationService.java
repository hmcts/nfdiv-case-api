package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ApplicationRejectedFeeNotPaidNotification;
import uk.gov.hmcts.divorce.common.notification.ApplicationWithdrawnNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseTerminationService {
    private final ApplicationWithdrawnNotification applicationWithdrawnNotification;

    private final ApplicationRejectedFeeNotPaidNotification applicationRejectedFeeNotPaidNotification;

    private final NotificationDispatcher notificationDispatcher;

    private final CcdAccessService ccdAccessService;

    public void withdraw(final CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        log.info("Withdrawing Case Id: {}", details.getId());

        removeAccessToCase(details);

        log.info("Case successfully withdrawn Case Id: {}", details.getId());

        notifyApplicantsOfCase(applicationWithdrawnNotification, details);
    }

    public void reject(final CaseDetails<CaseData, State> details) {
        log.info("Rejecting Case Id: {}", details.getId());

        removeAccessToCase(details);

        log.info("Case successfully rejected Case Id: {}", details.getId());

        notifyApplicantsOfCase(applicationRejectedFeeNotPaidNotification, details);
    }

    private void cancelInvitationToApplicant2(final CaseData caseData) {
        if (Objects.nonNull(caseData.getCaseInvite())) {
            caseData.setCaseInvite(new CaseInvite(caseData.getCaseInvite().applicant2InviteEmailAddress(), null, null));
        }
    }

    private void removeSolicitorOrganisationPolicy(final Applicant applicant) {
        if (applicant.isRepresented()) {
            applicant.getSolicitor().setOrganisationPolicy(null);
        }
    }

    private void unlinkApplicantsFromCcdCase(Long caseId) {
        ccdAccessService.removeUsersWithRole(caseId, List.of(CREATOR.getRole(), APPLICANT_2.getRole()));
    }

    private void notifyApplicantsOfCase(ApplicantNotification notification, final CaseDetails<CaseData, State> details) {
        notificationDispatcher.send(notification, details);
    }

    private void removeAccessToCase(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        cancelInvitationToApplicant2(caseData);
        removeSolicitorOrganisationPolicy(caseData.getApplicant1());
        removeSolicitorOrganisationPolicy(caseData.getApplicant2());
        unlinkApplicantsFromCcdCase(details.getId());
    }
}
