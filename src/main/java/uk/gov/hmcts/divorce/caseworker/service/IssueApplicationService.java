package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.divorce.caseworker.service.task.SetIssueDate;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private GenerateApplication generateApplication;

    @Autowired
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Autowired
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Autowired
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Autowired
    private SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    @Autowired
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Autowired
    private SetDueDateAfterIssue setDueDateAfterIssue;

    @Autowired
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Autowired
    private GenerateD10Form generateD10Form;

    @Autowired
    private GenerateD84Form generateD84Form;

    @Autowired
    private SetServiceType setServiceType;

    @Autowired
    private SetIssueDate setIssueDate;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setServiceType,
            setIssueDate,
            setPostIssueState,
            setDueDateAfterIssue,
            setNoticeOfProceedingDetailsForRespondent,
            generateApplicant1NoticeOfProceeding,
            generateApplicant2NoticeOfProceedings,
            divorceApplicationRemover,
                generateApplication,
            generateD10Form,
            generateD84Form
        ).run(caseDetails);
    }

    public void sendNotifications(final CaseDetails<CaseData, State> caseDetails) {
        caseTasks(
            sendAosPackToRespondent,
            sendAosPackToApplicant,
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
