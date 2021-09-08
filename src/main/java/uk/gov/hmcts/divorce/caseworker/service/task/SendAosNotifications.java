package uk.gov.hmcts.divorce.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.SolicitorServiceNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
public class SendAosNotifications implements CaseTask {

    @Autowired
    private SolicitorServiceNotification solicitorServiceNotification;

    @Autowired
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        if (caseDetails.getData().getApplication().isSolicitorApplication()) {

            final CaseData caseData = caseDetails.getData();
            final Long caseId = caseDetails.getId();

            if (caseData.getApplication().isSolicitorServiceMethod()) {
                solicitorServiceNotification.send(caseData, caseId);
            } else {
                noticeOfProceedingsNotification.send(caseData, caseId);
            }
        }

        return caseDetails;
    }
}
