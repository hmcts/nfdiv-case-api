package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerApplicantResponded implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_APPLICANT_RESPONDED = "caseworker-submit-from-awaitingdocuments-app1resp";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_APPLICANT_RESPONDED)
            .forStateTransition(AwaitingDocuments, Submitted)
            .showEventNotes()
            .name("Applicant responded")
            .description("Applicant responded")
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }
}
