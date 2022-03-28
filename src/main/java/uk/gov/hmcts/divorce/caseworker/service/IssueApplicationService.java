package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateNoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
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
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Autowired
    private Clock clock;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            details -> {
                final CaseData caseData = details.getData();
                final Applicant applicant1 = caseData.getApplicant1();
                final Applicant applicant2 = caseData.getApplicant2();
                if (!applicant1.isRepresented() && !applicant2.isRepresented() && applicant2.isBasedOverseas()) {
                    caseData.getApplication().setSolServiceMethod(PERSONAL_SERVICE);
                }
                caseData.getApplication().setIssueDate(LocalDate.now(clock));
                return details;
            },
            setPostIssueState,
            setDueDateAfterIssue,
            generateNoticeOfProceeding,
            generateRespondentAosInvitation,
            divorceApplicationRemover,
            generateDivorceApplication,
            sendAosPackToRespondent,
            sendAosPackToApplicant,
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
