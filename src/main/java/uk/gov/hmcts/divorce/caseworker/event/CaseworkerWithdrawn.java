package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ApplicationWithdrawnNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_EXCLUDING_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerWithdrawn implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_WITHDRAWN = "caseworker-withdrawn";

    @Autowired
    private ApplicationWithdrawnNotification applicationWithdrawnNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_WITHDRAWN)
            .forStateTransition(STATES_EXCLUDING_WITHDRAWN_AND_REJECTED, Withdrawn)
            .name("Withdraw")
            .description("Withdrawn")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker withdrawn about to submit callback invoked for Case Id: {}", details.getId());
        final CaseData caseData = details.getData();

        final var roles = List.of(CREATOR.getRole(), APPLICANT_2.getRole());

        if (Objects.nonNull(caseData.getCaseInvite())) {
            caseData.setCaseInvite(new CaseInvite(caseData.getCaseInvite().applicant2InviteEmailAddress(), null, null));
        }

        removeSolicitorOrganisationPolicy(caseData.getApplicant1());
        removeSolicitorOrganisationPolicy(caseData.getApplicant2());

        ccdAccessService.removeUsersWithRole(details.getId(), roles);

        if (caseData.getApplicant1().getGender() != null) {
            notificationDispatcher.send(applicationWithdrawnNotification, caseData, details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    private void removeSolicitorOrganisationPolicy(final Applicant applicant) {
        if (applicant.isRepresented()) {
            applicant.getSolicitor().setOrganisationPolicy(null);
        }
    }
}
