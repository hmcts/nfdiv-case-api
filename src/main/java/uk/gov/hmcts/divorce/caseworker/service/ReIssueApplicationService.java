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
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.MiniApplicationRemover;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class ReIssueApplicationService {

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

    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {

        ReissueOption reissueOption = caseDetails.getData().getApplication().getReissueOption();

        caseDetails.getData().getApplication().setReissueDate(LocalDate.now(clock));

        switch (reissueOption) {
            case DIGITAL_AOS:
                return caseTasks(
                    sendAosPack,
                    details -> {
                        details.getData().getApplication().setIssueDate(LocalDate.now(clock));
                        return details;
                    }
                ).run(caseDetails);
            break;

            case OFFLINE_AOS:
                return caseTasks(
                    sendAosPack,
                    details -> {
                        details.getData().getApplication().setIssueDate(LocalDate.now(clock));
                        details.getData().setDueDate(LocalDate.now(clock).plusDays(14));
                        return details;
                    }
                ).run(caseDetails);
                break;
            case REISSUE_CASE:
                return caseTasks(
                    sendAosPack,
                    details -> {
                        details.getData().getApplication().setIssueDate(LocalDate.now(clock));
                        details.getData().setDueDate(LocalDate.now(clock).plusDays(14));
                        return details;
                    }
                ).run(caseDetails);
                break;
            default:
                break;

        }
    }
}
