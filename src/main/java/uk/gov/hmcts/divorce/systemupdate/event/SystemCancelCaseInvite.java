package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SystemCancelCaseInvite implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_CANCEL_CASE_INVITE = "system-cancel-case-invite";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_CANCEL_CASE_INVITE)
            .forAllStates()
            .name("Cancel User Case Invite")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        long caseId = details.getId();
        log.info("{} aboutToSubmit callback invoked for case id: {}",  SYSTEM_CANCEL_CASE_INVITE, caseId);

        boolean app1InviteWasCancelled = app1InviteWasCancelled(details.getData(), beforeDetails.getData());
        log.info("Case invite cancelled by {} for case id: {}", app1InviteWasCancelled ? "Applicant 1" : "Respondent/Applicant 2", caseId);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private boolean app1InviteWasCancelled(CaseData data, CaseData beforeData) {
        return app1AccessCodePresent(beforeData) && !app1AccessCodePresent(data);
    }

    private boolean app1AccessCodePresent(CaseData data) {
        return Optional.ofNullable(data.getCaseInviteApp1())
            .map(CaseInviteApp1::accessCodeApplicant1)
            .isPresent();
    }
}
