package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";

    @Autowired
    private ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SYSTEM_PRONOUNCE_CASE)
            .forStateTransition(AwaitingPronouncement, ConditionalOrderPronounced)
            .name("System pronounce case")
            .description("System pronounce case")
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();
        log.info("Conditional order pronounced for Case({}), notifying Applicant 1 their conditional order has been pronounced", caseId);
        notificationDispatcher.send(conditionalOrderPronouncedNotification, caseData, details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
