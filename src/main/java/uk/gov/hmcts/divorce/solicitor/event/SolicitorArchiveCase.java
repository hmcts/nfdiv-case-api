package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PRE_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SolicitorArchiveCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_ARCHIVE_CASE = "solicitor-archive-case";
    private static final String ARCHIVE_CASE = "Archive Case";
    private static final String ARCHIVE_DRAFT_CASE_SOL_GUIDE = "Refer to the <a href=\"https://www.gov.uk/government/publications/myhmcts"
            + "-how-to-apply-online-for-a-divorce-or-dissolution/archive-a-draft-divorce-case\""
            + " target=\"_blank\" rel=\"noopener noreferrer\">Solicitor Guidance</a>";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        EnumSet<State> eventStates = EnumSet.copyOf(PRE_SUBMISSION_STATES);
        eventStates.add(Draft);

        new PageBuilder(configBuilder
            .event(SOLICITOR_ARCHIVE_CASE)
            .forStateTransition(eventStates, Archived)
            .name(ARCHIVE_CASE)
            .description(ARCHIVE_CASE)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(SUPER_USER)
        ).page("archiveCase")
            .label("archiveDraftCaseSolGuide", ARCHIVE_DRAFT_CASE_SOL_GUIDE);
    }
}
