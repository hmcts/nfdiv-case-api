package uk.gov.hmcts.divorce.common.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.SoleAosSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
public class SendCitizenAosNotifications implements CaseTask {

    @Autowired
    private SoleAosSubmittedNotification soleAosSubmittedNotification;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        if (data.getAcknowledgementOfService().isDisputed()) {
            if (!data.getApplicant1().isRepresented()) {
                soleAosSubmittedNotification.sendApplicationDisputedToApplicant(data, details.getId());
            }
            if (!data.getApplicant2().isRepresented()) {
                soleAosSubmittedNotification.sendApplicationDisputedToRespondent(data, details.getId());
            }
        } else {
            if (!data.getApplicant1().isRepresented()) {
                soleAosSubmittedNotification.sendApplicationNotDisputedToApplicant(data, details.getId());
            }
            if (!data.getApplicant2().isRepresented()) {
                soleAosSubmittedNotification.sendApplicationNotDisputedToRespondent(data, details.getId());
            }
        }

        return details;
    }
}

