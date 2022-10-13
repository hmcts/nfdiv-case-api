package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ApplyForFinalOrderDetails;
import uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressFinalOrderState;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class ApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String FINAL_ORDER_REQUESTED = "final-order-requested";

    public static final String APPLY_FOR_FINAL_ORDER = "Apply for final order";

    @Autowired
    private Applicant1AppliedForFinalOrderNotification applicant1AppliedForFinalOrderNotification;

    @Autowired
    private FinalOrderRequestedNotification finalOrderRequestedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ProgressFinalOrderState progressFinalOrderState;

    @Autowired
    private Clock clock;

    private static final List<CcdPageConfiguration> pages = List.of(
        new ApplyForFinalOrderDetails()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(FINAL_ORDER_REQUESTED)
            .forStates(AwaitingFinalOrder, AwaitingJointFinalOrder, FinalOrderOverdue)
            .name(APPLY_FOR_FINAL_ORDER)
            .description(APPLY_FOR_FINAL_ORDER)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_1_SOLICITOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                APPLICANT_2_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Apply for Final Order about to submit callback invoked for Case Id: {}", details.getId());

        if (AwaitingFinalOrder.equals(details.getState())) {
            FinalOrder finalOrder = details.getData().getFinalOrder();

            if (isNull(finalOrder.getApplicant1AppliedForFinalOrderFirst())
                && isNull(finalOrder.getApplicant2AppliedForFinalOrderFirst())) {
                finalOrder.setApplicant2AppliedForFinalOrderFirst(NO);
                finalOrder.setApplicant1AppliedForFinalOrderFirst(YES);
                finalOrder.setDateFinalOrderSubmitted(LocalDateTime.now(clock));
            }

            notificationDispatcher.send(applicant1AppliedForFinalOrderNotification, details.getData(), details.getId());
        }

        var updatedDetails = progressFinalOrderState.apply(details);

        if (FinalOrderRequested.equals(updatedDetails.getState())) {
            notificationDispatcher.send(finalOrderRequestedNotification, updatedDetails.getData(), details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedDetails.getData())
            .state(updatedDetails.getState())
            .build();
    }
}
