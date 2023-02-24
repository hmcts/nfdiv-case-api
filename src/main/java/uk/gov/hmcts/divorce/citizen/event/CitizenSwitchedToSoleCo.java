package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder.D84ApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
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

    @Autowired
    private SwitchToSoleService switchToSoleService;

    @Autowired
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Autowired
    private SwitchToSoleCoPrinter switchToSoleCoPrinter;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE_CO)
            .forStateTransition(EnumSet.of(ConditionalOrderPending, AwaitingLegalAdvisorReferral), AwaitingLegalAdvisorReferral)
            .name("SwitchedToSoleCO")
            .description("Application type switched to sole post CO submission")
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        Long caseId = details.getId();
        log.info("SwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YES);
        data.getLabelContent().setApplicationType(SOLE_APPLICATION);
        data.getConditionalOrder().setSwitchedToSole(YES);

        // triggered by citizen users
        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            notificationDispatcher.send(applicant1SwitchToSoleCoNotification, data, caseId);
        } else if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            notificationDispatcher.send(applicant2SwitchToSoleCoNotification, data, caseId);
            switchToSoleService.switchUserRoles(data, caseId);
            switchToSoleService.switchApplicantData(data);
        }

        // triggered by system update user coming from Offline Document Verified
        if (CO_D84.equals(data.getDocuments().getTypeOfDocumentAttached())
            && SWITCH_TO_SOLE.equals(data.getConditionalOrder().getD84ApplicationType())) {

            if (ConditionalOrder.D84WhoApplying.APPLICANT_2.equals(data.getConditionalOrder().getD84WhoApplying())) {
                if (!data.getApplication().isPaperCase()) {
                    switchToSoleService.switchUserRoles(data, caseId);
                }
                switchToSoleService.switchApplicantData(data);
            }

            switchToSoleCoPrinter.print(data, caseId, data.getApplicant1(), data.getApplicant2());
        }

        generateConditionalOrderAnswersDocument.apply(
            details,
            data.getApplicant1().getLanguagePreference()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
