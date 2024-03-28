package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ResendConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Slf4j
@Component
public class SendRegeneratedCOPronouncedCoverLetters implements CaseTask {

    @Autowired
    private ResendConditionalOrderPronouncedNotification resendCoverLetterNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        try {
            notificationDispatcher.send(resendCoverLetterNotification, data, details.getId());
            data.getApplication().setCoPronouncedForceConfidentialCoverLetterResentAgain(YES);
        } catch (final NotificationTemplateException e) {
            log.error("SystemResendCOPronouncedCoverLetter Notification failed with message: {}", e.getMessage(), e);
            data.getApplication().setCoPronouncedForceConfidentialCoverLetterResentAgain(NO);
        }

        return details;
    }
}

