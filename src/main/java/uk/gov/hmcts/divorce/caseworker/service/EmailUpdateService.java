package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;

@Service
@Slf4j
public class EmailUpdateService {

    @Autowired
    InviteApplicantToCaseNotification inviteApplicantToCaseNotification;
    @Autowired
    NotificationDispatcher notificationDispatcher;
    @Autowired
    CcdAccessService ccdAccessService;

    public CaseDetails<CaseData, State> processEmailUpdate(final CaseDetails<CaseData, State> caseDetails,
                                                      final CaseDetails<CaseData, State> beforeCaseDetails,
                                                      boolean isApplicant1) {

        final CaseData data = caseDetails.getData();
        final CaseData beforeData = beforeCaseDetails.getData();

        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        if (willApplicantBeMadeOffline(caseDetails, beforeCaseDetails, isApplicant1)) {
            log.info("Party will be made offline and any case access removed for Case Id: {}", caseDetails.getId());
            final var roles = (data.getApplicationType() == ApplicationType.SOLE_APPLICATION)
                ? isApplicant1 ? List.of(CREATOR.getRole()) : List.of(APPLICANT_2.getRole())
                : List.of(CREATOR.getRole(), APPLICANT_2.getRole());

            applicant.setOffline(YesOrNo.YES);
            if (data.getApplicationType() == ApplicationType.JOINT_APPLICATION) {
                partner.setOffline(YesOrNo.YES);
            }
            ccdAccessService.removeUsersWithRole(caseDetails.getId(), roles);

        } else {
            if (applicant.getEmail() == null || applicant.getEmail().isBlank()
                || applicant.isRepresented()) {
                return caseDetails;
            }

            boolean isRespondentAndSoleCaseNotIssued = data.getApplicationType().isSole()
                && data.getApplication().getIssueDate() == null
                && !isApplicant1;

            if (isRespondentAndSoleCaseNotIssued) {
                return caseDetails;
            }
            createCaseInvite(data, isApplicant1);
            sendInviteToApplicantEmail(data, caseDetails.getId(), isApplicant1);
        }
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

    private void sendInviteToApplicantEmail(final CaseData caseData, Long id, boolean isApplicant1) {
        inviteApplicantToCaseNotification.send(caseData, id, isApplicant1);
    }

    public boolean willApplicantBeMadeOffline(final CaseDetails<CaseData, State> caseDetails,
                                              final CaseDetails<CaseData, State> beforeCaseDetails,
                                              boolean isApplicant1) {
        final CaseData data = caseDetails.getData();
        final CaseData beforeData = beforeCaseDetails.getData();

        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant beforeApplicant = isApplicant1 ? beforeData.getApplicant1() : beforeData.getApplicant2();

        return isEmailBeingRemoved(beforeApplicant, applicant) && doesApplicantNeedToBeMadeOffline(data, isApplicant1);
    }

    private boolean isEmailBeingRemoved(final Applicant before, final Applicant after) {
        if (!StringUtils.isEmpty(before.getEmail()) && StringUtils.isEmpty(after.getEmail())) {
            return true;
        }
        return false;
    }

    private boolean doesApplicantNeedToBeMadeOffline(CaseData caseData, boolean isApplicant1) {
        final Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        final Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();
        ApplicationType applicationType = caseData.getApplicationType();

        if (applicationType == ApplicationType.SOLE_APPLICATION && applicant.isRepresented()) {
            return false;
        }
        if (applicationType == ApplicationType.JOINT_APPLICATION && (applicant.isRepresented() || partner.isRepresented())) {
            return false;
        }
        return true;
    }
}
