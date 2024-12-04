package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
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
public class CaseworkerCreateCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CASE_FLAG = "createFlags";
    private static final String ALWAYS_HIDE = "flagLauncher = \"ALWAYS_HIDE\"";

    @Autowired
    private CaseFlagsService caseFlagsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToStartCallback(this::aboutToStart)
            .name("Create Case Flag")
            .description("Create Case Flag")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER)
            .grantHistoryOnly(
                LEGAL_ADVISOR,
                JUDGE))
            .page("caseworkerCaseFlag")
            .pageLabel("Create Case Flags")
            .optional(CaseData::getCaseFlags, ALWAYS_HIDE, true, true)
            .complex(CaseData::getPartyFlags)
            .optional(PartyFlags::getApplicant1Flags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant2Flags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant1SolicitorFlags, ALWAYS_HIDE, true, true)
            .optional(PartyFlags::getApplicant2SolicitorFlags, ALWAYS_HIDE, true, true)
            .done()
            .optional(CaseData::getFlagLauncher,
                null, null, null, null, "#ARGUMENT(CREATE,VERSION2.1)");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_CASE_FLAG, details.getId());
        CaseData data = details.getData();

        caseFlagsService.setSupplementaryDataForCaseFlags(details.getId());
        caseFlagsService.initialiseCaseFlags(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
