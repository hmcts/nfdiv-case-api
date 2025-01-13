package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PartyFlags;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerManageCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_MANAGE_CASE_FLAG = "manageFlags";
    private static final String ALWAYS_HIDE = "internalFlagLauncher = \"ALWAYS_HIDE\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_MANAGE_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .showCondition("caseFlagsSetupComplete=\"Yes\"")
            .name("Manage flags")
            .description("Manage flags")
            .showEventNotes()
            .showSummary()
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, JUDGE))
            .page("caseworkerManageFlags")
            .pageLabel("Manage flags")
            .optional(CaseData::getCaseFlags, ALWAYS_HIDE, true, true)
            .complex(CaseData::getPartyFlags)
                .optional(PartyFlags::getApplicant1Flags, ALWAYS_HIDE, true, true)
                .optional(PartyFlags::getApplicant2Flags, ALWAYS_HIDE, true, true)
                .optional(PartyFlags::getApplicant1SolicitorFlags, ALWAYS_HIDE, true, true)
                .optional(PartyFlags::getApplicant2SolicitorFlags, ALWAYS_HIDE, true, true)
            .done()
            .optional(CaseData::getInternalFlagLauncher,
                null, null, null, null, "#ARGUMENT(UPDATE,VERSION2.1)");
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Flag updated")
            .build();
    }
}
