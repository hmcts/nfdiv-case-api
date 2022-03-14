package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.SoleAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CitizenFinalOrderDelayReason implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_FINAL_ORDER_DELAY_REASON = "citizen-final-order-delay-reason";

    @Autowired
    private SoleAppliedForFinalOrderNotification soleAppliedForFinalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_FINAL_ORDER_DELAY_REASON)
            .forState(FinalOrderOverdue)
            .name("Citizen FO delay reason")
            .description("Citizen final order delay reason")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen final order delay reason about to submit callback invoked");

        CaseData data = details.getData();

        if (data.getApplicationType().isSole()) {
            notificationDispatcher.send(soleAppliedForFinalOrderNotification, data, details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(FinalOrderRequested)
            .build();
    }
}

