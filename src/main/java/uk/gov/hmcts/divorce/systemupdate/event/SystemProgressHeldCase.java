package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.AwaitingConditionalOrderNotification;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class SystemProgressHeldCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PROGRESS_HELD_CASE = "system-progress-held-case";

    @Autowired
    private AwaitingConditionalOrderNotification conditionalOrderNotification;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_PROGRESS_HELD_CASE)
            .forStateTransition(Holding, AwaitingConditionalOrder)
            .name("Awaiting Conditional Order")
            .description("Progress held case to Awaiting Conditional Order")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        Long caseId = details.getId();
        boolean applicant1SolicitorRepresented = caseData.getApplicant1().isRepresented();

        if (applicant1SolicitorRepresented) {
            log.info("For case id {} applicant is represented by solicitor hence sending conditional order notification email", caseId);
            conditionalOrderNotification.sendToSolicitor(caseData, caseId);
        } else {
            log.info("20wk holding period elapsed for Case({}), notifying Joint Applicants they can apply for conditional order", caseId);
            conditionalOrderNotification.sendToApplicant1(caseData, caseId, false);
            if (!caseData.getApplicationType().isSole()) {
                conditionalOrderNotification.sendToApplicant2(caseData, caseId, false);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
