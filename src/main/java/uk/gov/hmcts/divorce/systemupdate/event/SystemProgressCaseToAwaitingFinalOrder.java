package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.AwaitingFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForFinalOrderDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD36Form;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemProgressCaseToAwaitingFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER = "system-progress-case-awaiting-final-order";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private AwaitingFinalOrderNotification awaitingFinalOrderNotification;

    @Autowired
    private GenerateD36Form generateD36Form;

    @Autowired
    private GenerateApplyForFinalOrderDocument generateApplyForFinalOrderDocument;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER)
            .forStateTransition(ConditionalOrderPronounced, AwaitingFinalOrder)
            .name("Awaiting Final Order")
            .description("Progress case to Awaiting Final Order")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        generateLetters(caseData, caseId);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("6 week 1 day period elapsed for Case({}), notifying applicant(s) that they can apply for final order", details.getId());

        notificationDispatcher.send(awaitingFinalOrderNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }

    private void generateLetters(final CaseData caseData, final Long caseId) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        if (caseData.getApplicant1().isApplicantOffline()) {
            log.info("Generating applicant 1 offline final order documents for CaseID: {}", caseId);

            generateD36Form.generateD36Document(caseData, caseId);
            generateApplyForFinalOrderDocument.generateApplyForFinalOrder(caseData, caseId, applicant1, applicant2);
        }

        if (!caseData.getApplicationType().isSole() && caseData.getApplicant2().isApplicantOffline()) {
            log.info("Generating applicant 2 offline final order documents for CaseID: {}", caseId);

            generateD36Form.generateD36Document(caseData, caseId);
            generateApplyForFinalOrderDocument.generateApplyForFinalOrder(caseData, caseId, applicant2, applicant1);
        }
    }
}
