package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.caseworker.service.task.*;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class EmailUpdateService {

    @Autowired
    private SendCaseInviteToApplicant1 sendCaseInviteToApplicant1;
    @Autowired
    private SendCaseInviteToApplicant2 sendCaseInviteToApplicant2;
    @Autowired
    private SetCaseInviteApplicant1 setCaseInviteApplicant1;
    @Autowired
    private SetCaseInviteApplicant1 setCaseInviteApplicant2;
    @Autowired
    private EmailUpdatedNotification emailUpdatedNotification;
    public CaseDetails<CaseData, State> processUpdateForApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setCaseInviteApplicant1,
            sendCaseInviteToApplicant1
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> processUpdateForApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setCaseInviteApplicant2,
            sendCaseInviteToApplicant2
        ).run(caseDetails);
    }

    public void sendNotificationToOldEmail(final CaseDetails<CaseData, State> caseDetails,
                                           String newEmail, boolean isApplicant1) {
        emailUpdatedNotification.send(caseDetails.getData(), caseDetails.getId(), newEmail, isApplicant1);
    }
}
