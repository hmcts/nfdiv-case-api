package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ApplicationRejectedFeeNotPaidNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemRejectCasesWithPaymentOverdue implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICATION_REJECTED_FEE_NOT_PAID = "application-rejected-fee-not-paid";
    private static final String APPLICATION_REJECTED = "Application rejected";

    private final NotificationDispatcher notificationDispatcher;
    private final ApplicationRejectedFeeNotPaidNotification applicationRejectedFeeNotPaidNotification;
    private final CcdAccessService ccdAccessService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(APPLICATION_REJECTED_FEE_NOT_PAID)
            .forStateTransition(AwaitingPayment, Rejected)
            .name(APPLICATION_REJECTED)
            .description(APPLICATION_REJECTED)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .ttlIncrement(180)
            .grant(CREATE_READ_UPDATE,
                SYSTEMUPDATE)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} aboutToSubmit callback invoked for case id: {}", APPLICATION_REJECTED_FEE_NOT_PAID, details.getId());

        CaseData caseData = details.getData();

        final var roles = List.of(CREATOR.getRole(), APPLICANT_2.getRole());

        if (Objects.nonNull(caseData.getCaseInvite())) {
            caseData.setCaseInvite(new CaseInvite(caseData.getCaseInvite().applicant2InviteEmailAddress(), null, null));
        }

        removeSolicitorOrganisationPolicy(caseData.getApplicant1());
        removeSolicitorOrganisationPolicy(caseData.getApplicant2());

        ccdAccessService.removeUsersWithRole(details.getId(), roles);

        caseData.getApplication().setPreviousState(details.getState());

        notificationDispatcher.send(applicationRejectedFeeNotPaidNotification, details.getData(), details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private void removeSolicitorOrganisationPolicy(final Applicant applicant) {
        if (applicant.isRepresented()) {
            applicant.getSolicitor().setOrganisationPolicy(null);
        }
    }
}
