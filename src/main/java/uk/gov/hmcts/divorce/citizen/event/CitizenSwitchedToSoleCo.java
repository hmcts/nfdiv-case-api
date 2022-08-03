package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSoleCo implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_CO = "switch-to-sole-co";

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Applicant1SwitchToSoleCoNotification applicant1SwitchToSoleCoNotification;

    @Autowired
    private Applicant2SwitchToSoleCoNotification applicant2SwitchToSoleCoNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE_CO)
            .forStateTransition(ConditionalOrderPending, AwaitingLegalAdvisorReferral)
            .name("SwitchedToSoleCO")
            .description("Application type switched to sole post CO submission")
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        Long caseId = details.getId();
        log.info("SwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YesOrNo.YES);

        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            notificationDispatcher.send(applicant1SwitchToSoleCoNotification, data, caseId);
        } else {
            notificationDispatcher.send(applicant2SwitchToSoleCoNotification, data, caseId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
