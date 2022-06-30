package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetReIssueAndDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;

import static java.lang.String.format;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
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
    private GenerateDivorceApplication generateDivorceApplication;

    @Autowired
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Autowired
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Autowired
    private SendAosPackToRespondent sendAosPackToRespondent;

    @Autowired
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Autowired
    private SetReIssueAndDueDate setReIssueAndDueDate;

    @Autowired
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Autowired
    private GenerateD10Form generateD10Form;

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
                setPostIssueState,
                setReIssueAndDueDate,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateD10Form,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (OFFLINE_AOS.equals(reissueOption)) {
            log.info("For case id {} processing reissue for offline aos ", caseDetails.getId());

            caseDetails.getData().getApplicant2().setOffline(YES);

            return caseTasks(
                setPostIssueState,
                setReIssueAndDueDate,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateDivorceApplication,
                generateD10Form,
                sendAosPackToRespondent,
                sendAosPackToApplicant,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (REISSUE_CASE.equals(reissueOption)) {
            log.info("For case id {} processing complete reissue ", caseDetails.getId());
            return caseTasks(
                setPostIssueState,
                setReIssueAndDueDate,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateDivorceApplication,
                generateD10Form,
                sendAosPackToRespondent,
                sendAosPackToApplicant,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else {
            log.info("For case id {} invalid reissue option hence not processing reissue application ", caseDetails.getId());
            throw new InvalidReissueOptionException(format("Invalid reissue option for CaseId: %s", caseDetails.getId()));
        }

    }
}
