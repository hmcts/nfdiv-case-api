package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorInviteApplicant2 implements CCDConfig<CaseData, State, UserRole> {
    public static final String SOLICITOR_INVITE_APPLICANT_2 = "solicitor-invite-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_INVITE_APPLICANT_2)
            .forStates(Draft)
            .showCondition("applicant2SolicitorRepresented=\"Yes\" AND applicationType=\"jointApplication\"")
            .name("Invite Applicant 2")
            .description("Invite Applicant 2")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                SUPER_USER,
                CASE_WORKER,
                LEGAL_ADVISOR));
    }
}
