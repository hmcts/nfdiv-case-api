package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.AosReminderNotifications;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static java.util.EnumSet.of;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@RequiredArgsConstructor
@Component
public class SystemProgressCaseToAosOverdue implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PROGRESS_TO_AOS_OVERDUE = "system-progress-to-aos-overdue";

    private final NotificationDispatcher notificationDispatcher;

    private final AosReminderNotifications aosReminderNotifications;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_PROGRESS_TO_AOS_OVERDUE)
            .forStateTransition(of(AwaitingAos, AosDrafted), AosOverdue)
            .name("AoS not received within SLA")
            .description("AoS not received within SLA")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        notificationDispatcher.send(aosReminderNotifications, data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
