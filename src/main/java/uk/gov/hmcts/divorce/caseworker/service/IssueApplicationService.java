package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateNoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private GenerateDivorceApplication generateDivorceApplication;

    @Autowired
    private GenerateRespondentAosInvitation generateRespondentAosInvitation;

    @Autowired
    private GenerateNoticeOfProceeding generateNoticeOfProceeding;

    @Autowired
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Autowired
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Autowired
    private SetDueDateAfterIssue setDueDateAfterIssue;

    @Autowired
    private Clock clock;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setPostIssueState,
            details -> {
                details.getData().getApplication().setIssueDate(LocalDate.now(clock));
                return details;
            },
            setDueDateAfterIssue,
            generateNoticeOfProceeding,
            generateRespondentAosInvitation,
            divorceApplicationRemover,
            generateDivorceApplication,
            sendAosPackToRespondent,
            // TODO add sendNoticeOfProceedings (and application) to applicant
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
