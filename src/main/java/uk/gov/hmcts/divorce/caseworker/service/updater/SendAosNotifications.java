package uk.gov.hmcts.divorce.caseworker.service.updater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.PersonalServiceNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;

@Component
public class SendAosNotifications implements CaseDataAction {

    @Autowired
    private PersonalServiceNotification personalServiceNotification;

    @Autowired
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Override
    public CaseDataContext apply(CaseDataContext caseDataContext) {

        final CaseData caseData = caseDataContext.getCaseData();
        final Long caseId = caseDataContext.getCaseId();

        if (caseData.getApplication().isPersonalServiceMethod()) {
            personalServiceNotification.send(caseData, caseId);
        } else {
            noticeOfProceedingsNotification.send(caseData, caseId);
        }

        return caseDataContext;
    }
}
