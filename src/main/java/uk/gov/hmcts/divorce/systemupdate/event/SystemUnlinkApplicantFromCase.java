package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SystemUnlinkApplicantFromCase implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private CcdAccessService ccdAccessService;

    public static final String SYSTEM_UNLINK_APPLICANT = "system-unlink-applicant";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_UNLINK_APPLICANT)
            .forStates(Draft)
            .name("Unlink Applicant from case")
            .grant(CREATE_READ_UPDATE, CITIZEN, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("System unlink applicant from case: Case Id: {}", details.getId());
        CaseData caseData = details.getData();
        caseData.getApplicant1().setEmail(null);
        ccdAccessService.removeUsersWithRole(details.getId(), List.of(CREATOR.getRole()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
