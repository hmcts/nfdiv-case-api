package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.ResetAosFields;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToApplicant;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosPackToRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetReIssueAndDueDate;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.JudicialSeparationReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static java.lang.String.format;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class ReIssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

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
    private SetReIssueAndDueDate setReIssueAndDueDate;

    @Autowired
    private SendAosPackToApplicant sendAosPackToApplicant;

    @Autowired
    private ResetAosFields resetAosFields;

    @Autowired
    private GenerateD10Form generateD10Form;

    @Autowired
    private GenerateD84Form generateD84Form;

    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {
        if (caseDetails.getData().isJudicialSeparationCase()) {
            JudicialSeparationReissueOption jsReissueOption = caseDetails.getData().getApplication().getJudicialSeparationReissueOption();
            switch (jsReissueOption) {
                case OFFLINE_AOS -> caseDetails.getData().getApplication().setReissueOption(OFFLINE_AOS);
                case REISSUE_CASE -> caseDetails.getData().getApplication().setReissueOption(REISSUE_CASE);
                default -> caseDetails.getData().getApplication().setReissueOption(null);
            }
            caseDetails.getData().getApplication().setJudicialSeparationReissueOption(null);
        }
        ReissueOption reissueOption = caseDetails.getData().getApplication().getReissueOption();

        log.info("For case id {} reissue option selected is {} ", caseDetails.getId(), reissueOption);

        var updatedCaseDetails = updateCase(caseDetails, reissueOption);

        //Reset reissue option
        updatedCaseDetails.getData().getApplication().setPreviousReissueOption(reissueOption);
        updatedCaseDetails.getData().getApplication().setReissueOption(null);

        return updatedCaseDetails;
    }

    private CaseDetails<CaseData, State> updateCase(CaseDetails<CaseData, State> caseDetails, ReissueOption reissueOption) {
        if (DIGITAL_AOS.equals(reissueOption)) {
            log.info("For case id {} processing reissue for digital aos ", caseDetails.getId());

            caseDetails.getData().getApplicant2().setOffline(NO);

            return caseTasks(
                setPostIssueState,
                setReIssueAndDueDate,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateD10Form,
                resetAosFields
            ).run(caseDetails);
        } else if (OFFLINE_AOS.equals(reissueOption)) {
            log.info("For case id {} processing reissue for offline aos ", caseDetails.getId());

            caseDetails.getData().getApplicant2().setOffline(YES);

            return caseTasks(
                setPostIssueState,
                setReIssueAndDueDate,
                setNoticeOfProceedingDetailsForRespondent,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateApplication,
                generateD10Form,
                generateD84Form,
                resetAosFields
            ).run(caseDetails);
        } else if (REISSUE_CASE.equals(reissueOption)) {
            log.info("For case id {} processing complete reissue ", caseDetails.getId());
            return caseTasks(
                setPostIssueState,
                setReIssueAndDueDate,
                setNoticeOfProceedingDetailsForRespondent,
                generateApplicant1NoticeOfProceeding,
                generateApplicant2NoticeOfProceedings,
                generateApplication,
                generateD10Form,
                generateD84Form,
                resetAosFields
            ).run(caseDetails);
        } else {
            log.info("For case id {} invalid reissue option hence not processing reissue application ", caseDetails.getId());
            throw new InvalidReissueOptionException(format("Invalid reissue option for CaseId: %s", caseDetails.getId()));
        }
    }

    public void sendNotifications(CaseDetails<CaseData, State> caseDetails, ReissueOption reissueOption) {
        if (DIGITAL_AOS.equals(reissueOption)) {
            log.info("For case id {} sending reissue notifications for digital aos ", caseDetails.getId());
            caseTasks(
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (OFFLINE_AOS.equals(reissueOption)) {
            log.info("For case id {} sending reissue notifications for offline aos ", caseDetails.getId());

            caseTasks(
                sendAosPackToRespondent,
                sendAosPackToApplicant,
                sendApplicationIssueNotifications
            ).run(caseDetails);
        } else if (REISSUE_CASE.equals(reissueOption)) {
            log.info("For case id {} sending reissue notifications for reissue case", caseDetails.getId());
            caseTasks(
                sendAosPackToRespondent,
                sendAosPackToApplicant
            ).run(caseDetails);

            if (!caseDetails.getData().getApplication().getServiceMethod().equals(PERSONAL_SERVICE)) {
                sendApplicationIssueNotifications.apply(caseDetails);
            }

        } else {
            log.info("For case id {} invalid reissue option hence not sending reissue notifications ", caseDetails.getId());
            throw new InvalidReissueOptionException(
                "Exception occurred while sending reissue application notifications for case id " + caseDetails.getId()
            );
        }

    }
}
