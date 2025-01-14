package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerPrepareForCaseFlags implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_PREPARE_FOR_CASEFLAGS = "caseworker-prepare-caseflags";

    @Autowired
    private CaseFlagsService caseFlagsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PREPARE_FOR_CASEFLAGS)
            .forAllStates()
            .name("Prepare for Case flags")
            .description("Prepare for Case flags")
            .showCondition("caseFlagsSetupComplete!=\"Yes\"")
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("prepareForCaseFlags")
            .pageLabel("Prepare for Case flags");
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_PREPARE_FOR_CASEFLAGS, details.getId());

        caseFlagsService.setSupplementaryDataForCaseFlags(details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
