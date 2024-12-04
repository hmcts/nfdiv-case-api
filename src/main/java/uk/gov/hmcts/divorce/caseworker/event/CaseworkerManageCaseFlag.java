package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PartyFlags;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerManageCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_MANAGE_CASE_FLAG = "manageFlags";
    private static final String ALWAYS_HIDE = "flagLauncher = \"ALWAYS_HIDE\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_MANAGE_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .name("Manage Case Flag")
            .description("Manage Case Flag")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER)
            .grantHistoryOnly(
                LEGAL_ADVISOR,
                JUDGE))
            .page("caseworkerManageCaseFlag")
            .pageLabel("Manage Case Flags")
            .optional(CaseData::getCaseFlags, ALWAYS_HIDE, true, true)
            .complex(CaseData::getPartyFlags)
            .optional(PartyFlags::getApplicant1Flags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant2Flags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant1SolicitorFlags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant2SolicitorFlags, ALWAYS_HIDE, true, true)
            .done()
            .optional(CaseData::getFlagLauncher,
                null, null, null, null, "#ARGUMENT(UPDATE,VERSION2.1)");
    }
}
