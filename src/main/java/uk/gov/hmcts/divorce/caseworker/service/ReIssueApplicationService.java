package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCitizenRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateMiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPack;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class ReIssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

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
    private Clock clock;

    @Value("${resissue.days-until-overdue}")
    private Integer daysUntilOverdue;

    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {
        ReissueOption reissueOption = caseDetails.getData().getApplication().getReissueOption();

        log.info("For case id {} reissue option selected is {} ", caseDetails.getId(), reissueOption.getLabel());

        caseDetails.getData().getApplication().setReissueDate(now(clock));

        CaseDetails<CaseData, State> updatedCaseDetails = null;

        switch (reissueOption) {
            case DIGITAL_AOS:
                log.info("For case id {} processing reissue for digital aos ", caseDetails.getId());
                updatedCaseDetails = caseTasks(
                    generateRespondentSolicitorAosInvitation,
                    generateCitizenRespondentAosInvitation,
                    sendAosNotifications,
                    details -> {
                        details.getData().getApplication().setReissueDate(now(clock));
                        details.getData().setDueDate(now(clock).plusDays(daysUntilOverdue));
                        return details;
                    },
                    setPostIssueState,
                    sendApplicationIssueNotifications
                ).run(caseDetails);
                break;

            case OFFLINE_AOS:
                log.info("For case id {} processing reissue for offline aos ", caseDetails.getId());
                updatedCaseDetails = caseTasks(
                    generateRespondentSolicitorAosInvitation,
                    generateCitizenRespondentAosInvitation,
                    sendAosPack,
                    details -> {
                        details.getData().getApplication().setReissueDate(now(clock));
                        details.getData().setDueDate(now(clock).plusDays(daysUntilOverdue));
                        return details;
                    },
                    setPostIssueState,
                    sendApplicationIssueNotifications
                ).run(caseDetails);
                break;

            case REISSUE_CASE:
                log.info("For case id {} processing complete reissue ", caseDetails.getId());
                updatedCaseDetails = caseTasks(
                    generateMiniApplication,
                    generateRespondentSolicitorAosInvitation,
                    generateCitizenRespondentAosInvitation,
                    sendAosNotifications,
                    sendAosPack,
                    details -> {
                        details.getData().getApplication().setReissueDate(now(clock));
                        details.getData().setDueDate(now(clock).plusDays(daysUntilOverdue));
                        return details;
                    },
                    setPostIssueState,
                    sendApplicationIssueNotifications
                ).run(caseDetails);
                break;

            default:
                log.info("For case id {} invalid reissue option hence not processing reissue application ", caseDetails.getId());
                break;

        }

        //Reset reissue option
        updatedCaseDetails.getData().getApplication().setReissueOption(null);

        return updatedCaseDetails;
    }
}
