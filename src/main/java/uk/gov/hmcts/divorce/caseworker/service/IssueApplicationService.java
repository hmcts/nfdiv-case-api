package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.divorce.caseworker.service.task.ValidateIssue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueApplicationService {

    private final SetPostIssueState setPostIssueState;

    private final DivorceApplicationRemover divorceApplicationRemover;

    private final GenerateApplication generateApplication;

    private final GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    private final GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    private final SendAosPackToRespondent sendAosPackToRespondent;

    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    private final SendApplicationIssueNotifications sendApplicationIssueNotifications;

    private final SetDueDateAfterIssue setDueDateAfterIssue;

    private final SendAosPackToApplicant sendAosPackToApplicant;

    private final GenerateD10Form generateD10Form;

    private final GenerateD84Form generateD84Form;

    private final SetServiceType setServiceType;

    private final SetIssueDate setIssueDate;

    private final ValidateIssue validateIssue;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setServiceType,
            validateIssue,
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
