package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.HearingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingOutcome;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSendHearingReminder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_SEND_HEARING_REMINDER = "system-send-hearing-reminder";
    private final NotificationDispatcher notificationDispatcher;
    private final HearingReminderNotification hearingReminderNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_SEND_HEARING_REMINDER)
            .forState(PendingHearingOutcome)
            .name("Send Hearing Reminder")
            .description("Send Hearing Reminder")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final long caseId = details.getId();
        final CaseData caseData = details.getData();
        final Hearing hearing = caseData.getHearing();

        log.info("{} about to submit callback invoked for case: {}", SYSTEM_SEND_HEARING_REMINDER, caseId);

        if (YesOrNo.YES.equals(hearing.getHasHearingReminderBeenSent())) {
            log.error("{} errored as case has already been processed: {}", SYSTEM_SEND_HEARING_REMINDER, caseId);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CASE_ALREADY_PROCESSED_ERROR))
                .build();
        }

        hearing.setHasHearingReminderBeenSent(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        final CaseDetails<CaseData, State> details, final CaseDetails<CaseData, State> beforeDetails
    ) {
        final CaseData caseData = details.getData();
        final long caseId = details.getId();

        log.info("{} submitted callback invoked CaseID: {}", SYSTEM_SEND_HEARING_REMINDER, details.getId());

        notificationDispatcher.send(hearingReminderNotification, caseData, caseId);

        return SubmittedCallbackResponse.builder().build();
    }
}
