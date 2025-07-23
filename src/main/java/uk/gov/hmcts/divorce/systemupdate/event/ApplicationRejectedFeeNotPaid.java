package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ApplicationRejectedFeeNotPaidNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_DRAFT_OR_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationRejectedFeeNotPaid implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICATION_REJECTED_FEE_NOT_PAID = "application-rejected-fee-not-paid";
    private static final String APPLICATION_REJECTED = "Application rejected";

    private final NotificationDispatcher notificationDispatcher;
    private final ApplicationRejectedFeeNotPaidNotification applicationRejectedFeeNotPaidNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(APPLICATION_REJECTED_FEE_NOT_PAID)
            .forStateTransition(STATES_NOT_DRAFT_OR_WITHDRAWN_OR_REJECTED, Rejected)
            .name(APPLICATION_REJECTED)
            .description(APPLICATION_REJECTED)
            .submittedCallback(this::submitted)
            .ttlIncrement(180)
            .grant(CREATE_READ_UPDATE,
                SYSTEMUPDATE)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case id: {}", APPLICATION_REJECTED_FEE_NOT_PAID, details.getId());

        notificationDispatcher.send(applicationRejectedFeeNotPaidNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
