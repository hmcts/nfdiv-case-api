package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUnlinkApplicant2FromCase implements CCDConfig<CaseData, State, UserRole> {

    private final CcdAccessService ccdAccessService;

    public static final String ADMIN_UNLINK_APPLICANT_2 = "admin-unlink-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (Boolean.parseBoolean(System.getenv().get("ADMIN_UNLINK_APPLICANT_2_ENABLED"))) {
            configBuilder
                .event(ADMIN_UNLINK_APPLICANT_2)
                .forStates(POST_SUBMISSION_STATES)
                .name("Unlink Applicant2 from case")
                .grant(CREATE_READ_UPDATE, SUPER_USER)
                .retries(120, 120)
                .aboutToSubmitCallback(this::aboutToSubmit);
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();


        log.info("Admin unlinking Applicant2/Respondent from case (id: {})",  details.getId());
        ccdAccessService.removeUsersWithRole(details.getId(), List.of(APPLICANT_2.getRole()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
