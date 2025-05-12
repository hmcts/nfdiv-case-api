package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_DRAFT_OR_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerPrepareForCaseFlags implements CCDConfig<CaseData, State, UserRole> {
    private static final String PREPARE_FOR_CASE_FLAGS = "Prepare for Case flags";
    public static final String CASEWORKER_PREPARE_FOR_CASEFLAGS = "caseworker-prepare-caseflags";

    private final CaseFlagsService caseFlagsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PREPARE_FOR_CASEFLAGS)
            .forStates(STATES_NOT_DRAFT_OR_WITHDRAWN_OR_REJECTED)
            .name(PREPARE_FOR_CASE_FLAGS)
            .description(PREPARE_FOR_CASE_FLAGS)
            .showCondition("caseFlagsSetupComplete!=\"Yes\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("prepareForCaseFlags")
            .pageLabel(PREPARE_FOR_CASE_FLAGS);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        final CaseData caseData = details.getData();
        caseFlagsService.initialiseCaseFlags(caseData);
        caseData.setCaseFlagsSetupComplete(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_PREPARE_FOR_CASEFLAGS, details.getId());

        caseFlagsService.setSupplementaryDataForCaseFlags(details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
