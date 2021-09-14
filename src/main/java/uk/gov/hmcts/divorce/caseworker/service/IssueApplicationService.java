package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCitizenRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateMiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPack;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDate;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.MiniApplicationRemover;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private GenerateMiniApplication generateMiniApplication;

    @Autowired
    private GenerateRespondentSolicitorAosInvitation generateRespondentSolicitorAosInvitation;

    @Autowired
    private GenerateCitizenRespondentAosInvitation generateCitizenRespondentAosInvitation;

    @Autowired
    private SendAosPack sendAosPack;

    @Autowired
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Autowired
    private SendAosNotifications sendAosNotifications;

    @Autowired
    private SetDueDate setDueDate;

    @Autowired
    private Clock clock;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setPostIssueState,
            generateRespondentSolicitorAosInvitation,
            generateCitizenRespondentAosInvitation,
            miniApplicationRemover,
            generateMiniApplication,
            sendAosPack,
            sendAosNotifications,
            setDueDate,
            details -> {
                details.getData().getApplication().setIssueDate(LocalDate.now(clock));
                return details;
            },
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
