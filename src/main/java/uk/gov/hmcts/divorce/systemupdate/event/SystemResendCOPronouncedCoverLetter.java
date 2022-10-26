package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ResendConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoversheet;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemResendCOPronouncedCoverLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER = "system-resend-co-pronounced-letter";

    @Autowired
    private ResendConditionalOrderPronouncedNotification resendCoverLetterNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private RegenerateConditionalOrderPronouncedCoversheet regenerateCoverSheet;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(
            configBuilder
                .event(SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER)
                .forState(ConditionalOrderPronounced)
                .name("Resend CO Pronounced letter")
                .description("Resend CO Pronounced letter")
                .showCondition(NEVER_SHOW)
                .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
                .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        log.info("SystemResendCOPronouncedCoverLetter about to submit callback invoked for case id: {}", caseId);

        regenerateCoverSheet.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("SystemResendCOPronouncedCoverLetter submitted callback invoked for case id: {}", details.getId());

        try {
            notificationDispatcher.send(resendCoverLetterNotification, details.getData(), details.getId());
        } catch (final NotificationTemplateException e) {
            log.error("Notification failed with message: {}", e.getMessage(), e);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
