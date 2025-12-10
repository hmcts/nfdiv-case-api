package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ApplicationWithdrawnNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawCaseService {
    private final ApplicationWithdrawnNotification applicationWithdrawnNotification;

    private final NotificationDispatcher notificationDispatcher;

    private final CcdAccessService ccdAccessService;

    public void withdraw(final CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        log.info("Withdrawing Case Id: {}", details.getId());

        cancelInvitationToApplicant2(caseData);
        removeSolicitorOrganisationPolicy(caseData.getApplicant1());
        removeSolicitorOrganisationPolicy(caseData.getApplicant2());
        unlinkApplicantsFromCcdCase(details.getId());

        log.info("Case successfully withdrawn Case Id: {}", details.getId());

        notifyApplicantsOfCaseWithdrawal(caseData, details);
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

    private void notifyApplicantsOfCaseWithdrawal(final CaseData data, final CaseDetails<CaseData, State> details) {
        boolean isTriggeredByCaseworker = data.getApplication().getCwWithdrawApplicationReason() != null;

        if (!isTriggeredByCaseworker) {
            notificationDispatcher.send(applicationWithdrawnNotification, details);
        }
    }
}
