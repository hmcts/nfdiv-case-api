package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ResendJudicialSeparationCitizenAosResponseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResendJSCitizenAOSResponseLetters implements CaseTask {

    public static final String NOTIFICATION_TEMPLATE_ERROR = "SystemResendJSCitizenAosResponseLetter Notification failed with message: {}";
    private final ResendJudicialSeparationCitizenAosResponseNotification resendJudicialSeparationCitizenAosResponseNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        try {
            notificationDispatcher.send(resendJudicialSeparationCitizenAosResponseNotification, data, details.getId());
            data.getApplication().setJsCitizenAosResponseLettersResent(YES);
        } catch (final NotificationTemplateException e) {
            log.error(NOTIFICATION_TEMPLATE_ERROR, e.getMessage(), e);
            data.getApplication().setJsCitizenAosResponseLettersResent(NO);
        }

        return details;
    }
}

