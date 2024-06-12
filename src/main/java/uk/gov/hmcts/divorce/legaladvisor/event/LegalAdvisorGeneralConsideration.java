package uk.gov.hmcts.divorce.legaladvisor.event;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferral.CASEWORKER_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ExpeditedCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;

@Component
@Slf4j
public class LegalAdvisorGeneralConsideration implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_GENERAL_CONSIDERATION = "legal-advisor-general-consideration";

    @Autowired
    private IdamService idamService;

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private Clock clock;

    @Autowired
    private HttpServletRequest httpServletRequest;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_GENERAL_CONSIDERATION)
            .forStates(AwaitingGeneralConsideration, ExpeditedCase)
            .name("General Consideration")
            .description("General Consideration")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR, JUDGE)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER))
            .page("generalConsiderationResponse")
            .pageLabel("General consideration response")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralReferralDecision)
                .mandatory(GeneralReferral::getGeneralReferralDecisionReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Legal advisor general consideration about to submit callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();
        final GeneralReferral copyOfGeneralReferral = caseData.getGeneralReferral().toBuilder().build();

        copyOfGeneralReferral.setGeneralReferralDecisionDate(LocalDate.now(clock));

        final ListValue<GeneralReferral> generalReferralListValue = ListValue.<GeneralReferral>builder()
            .id(UUID.randomUUID().toString())
            .value(copyOfGeneralReferral)
            .build();

        if (isNull(caseData.getGeneralReferrals())) {
            caseData.setGeneralReferrals(singletonList(generalReferralListValue));
        } else {
            caseData.getGeneralReferrals().add(0, generalReferralListValue);
        }

        // Reset all fields apart from urgent case flag as it is still required by agents to filter cases.
        caseData.setGeneralReferral(
            GeneralReferral
                .builder()
                .generalReferralUrgentCase(caseData.getGeneralReferral().getGeneralReferralUrgentCase())
                .build()
        );

        State  endState = GeneralConsiderationComplete;

        if (details.getState().equals(ExpeditedCase) && isJudge(details.getId())) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();
            final Long caseId = details.getId();

            log.info("CaseID {} Expedited case.  Triggering Legal advisor/Judge make decision event.", details.getId());

            ccdUpdateService.submitEvent(caseId, LEGAL_ADVISOR_MAKE_DECISION, user, serviceAuthorization);
            endState = ExpeditedCase;
        } else {
            log.info("CaseID {} Does not meet legal advisor make decision event requirements.  Skipping event.", details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public boolean isJudge(Long caseId) {
        log.info("Retrieving roles for user on case {}", caseId);
        final String userAuth = httpServletRequest.getHeader(AUTHORIZATION);
        User user = idamService.retrieveUser(userAuth);
        List<String> userRoles =
                caseAssignmentApi.getUserRoles(
                                user.getAuthToken(),
                                authTokenGenerator.generate(),
                                List.of(String.valueOf(caseId)),
                                List.of(user.getUserDetails().getUid())
                        )
                        .getCaseAssignmentUserRoles()
                        .stream()
                        .map(CaseAssignmentUserRole::getCaseRole)
                        .collect(Collectors.toList());
        return userRoles.contains(JUDGE.getRole());
    }
}
