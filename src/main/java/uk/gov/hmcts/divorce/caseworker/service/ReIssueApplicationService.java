package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCitizenRespondentAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPack;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetReIssueAndDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.ReissueProcessingException;

import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class ReIssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

    @Autowired
    private GenerateDivorceApplication generateMiniApplication;

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
    private SetReIssueAndDueDate setReIssueAndDueDate;

    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {
        ReissueOption reissueOption = caseDetails.getData().getApplication().getReissueOption();

        log.info("For case id {} reissue option selected is {} ", caseDetails.getId(), reissueOption);

        var updatedCaseDetails = updateCase(caseDetails, reissueOption);

        //Reset reissue option
        updatedCaseDetails.getData().getApplication().setReissueOption(null);

        return updatedCaseDetails;
    }

    private CaseDetails<CaseData, State> updateCase(CaseDetails<CaseData, State> caseDetails, ReissueOption reissueOption) {
        if (DIGITAL_AOS.equals(reissueOption)) {
            log.info("For case id {} processing reissue for digital aos ", caseDetails.getId());
            return caseTasks(
                generateRespondentSolicitorAosInvitation,
                generateCitizenRespondentAosInvitation,
                sendAosNotifications,
                setReIssueAndDueDate,
                setPostIssueState,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (OFFLINE_AOS.equals(reissueOption)) {
            log.info("For case id {} processing reissue for offline aos ", caseDetails.getId());
            return caseTasks(
                generateRespondentSolicitorAosInvitation,
                generateCitizenRespondentAosInvitation,
                sendAosPack,
                setReIssueAndDueDate,
                setPostIssueState,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (REISSUE_CASE.equals(reissueOption)) {
            log.info("For case id {} processing complete reissue ", caseDetails.getId());
            return caseTasks(
                generateMiniApplication,
                generateRespondentSolicitorAosInvitation,
                generateCitizenRespondentAosInvitation,
                sendAosNotifications,
                sendAosPack,
                setReIssueAndDueDate,
                setPostIssueState,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else {
            log.info("For case id {} invalid reissue option hence not processing reissue application ", caseDetails.getId());
            throw new ReissueProcessingException(
                "Exception occurred while processing reissue application for case id " + caseDetails.getId()
            );
        }

    }
}
