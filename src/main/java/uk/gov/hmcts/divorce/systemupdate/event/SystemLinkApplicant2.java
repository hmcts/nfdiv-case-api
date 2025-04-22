package uk.gov.hmcts.divorce.systemupdate.event;

import jakarta.servlet.http.HttpServletRequest;
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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_FOR_LINKING_APP2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SystemLinkApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public static final String SYSTEM_LINK_APPLICANT_2 = "system-link-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_LINK_APPLICANT_2)
            .forStates(STATES_FOR_LINKING_APP2)
            .name("Link Resp or App 2 to case")
            .description("Link Respondent or Applicant 2 to case so they are online")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        log.info("Linking Applicant 2 to Case");
        ccdAccessService.linkRespondentToApplication(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId(),
            data.getCaseInvite().applicant2UserId()
        );

        data.setCaseInvite(data.getCaseInvite().useAccessCode());
        data.getApplicant2().setOffline(NO);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
