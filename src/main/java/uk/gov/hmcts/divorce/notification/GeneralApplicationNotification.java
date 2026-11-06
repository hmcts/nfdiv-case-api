package uk.gov.hmcts.divorce.notification;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public interface GeneralApplicationNotification {
    void sendToApplicant(CaseData caseData, Long caseId, GeneralApplication generalApplication);
}
