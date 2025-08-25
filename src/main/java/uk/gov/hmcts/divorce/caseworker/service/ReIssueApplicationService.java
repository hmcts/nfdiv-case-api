package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.JudicialSeparationReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrAddress;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrAddress.EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse.SEND_PAPERS_AGAIN;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReIssueApplicationService {

    private final SetPostIssueState setPostIssueState;

    private final GenerateApplication generateApplication;

    private final GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    private final GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    private final SendAosPackToRespondent sendAosPackToRespondent;

    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    private final SendApplicationIssueNotifications sendApplicationIssueNotifications;

    private final SetReIssueAndDueDate setReIssueAndDueDate;

    private final SendAosPackToApplicant sendAosPackToApplicant;

    private final ResetAosFields resetAosFields;

    private final GenerateD10Form generateD10Form;

    private final GenerateD84Form generateD84Form;

    private final SetServiceType setServiceType;


    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();

        if (caseData.isJudicialSeparationCase()) {
            JudicialSeparationReissueOption jsReissueOption = caseData.getApplication().getJudicialSeparationReissueOption();
            switch (jsReissueOption) {
                case OFFLINE_AOS -> caseData.getApplication().setReissueOption(OFFLINE_AOS);
                case REISSUE_CASE -> caseData.getApplication().setReissueOption(REISSUE_CASE);
                default -> caseData.getApplication().setReissueOption(null);
            }
            caseData.getApplication().setJudicialSeparationReissueOption(null);
        }
        ReissueOption reissueOption = caseData.getApplication().getReissueOption();

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

    public void updateReissueOptionForNewContactDetails(CaseDetails<CaseData, State> caseDetails, Long caseId) {

        CaseData caseData = caseDetails.getData();

        var noResponseOptions =
            Optional.of(caseData.getApplicant1())
                .map(Applicant::getInterimApplicationOptions)
                .map(InterimApplicationOptions::getNoResponseJourneyOptions)
                .orElse(new NoResponseJourneyOptions());

        boolean isNewAddressOverseas = YES.equals(noResponseOptions.getNoResponsePartnerAddressOverseas());
        boolean isCurrentAddressOverseas = caseData.getApplicant2().isBasedOverseas()
            || caseData.getApplicant2().getAddressOverseas() == YES;

        ReissueOption reissueOption = null;

        // Option 1
//
//        var isCourtService = caseData.getApplication().isCourtServiceMethod();
//        var emailPresent = !StringUtils.isEmpty(caseData.getApplicant2().getEmail());

//        if (SEND_PAPERS_AGAIN.equals(noResponseOptions.getNoResponseSendPapersAgainOrTrySomethingElse())) {
//            reissueOption = isCourtService && emailPresent ? OFFLINE_AOS : !emailPresent ? REISSUE_CASE : DIGITAL_AOS;;
//        } else {
//            switch (noResponseOptions.getNoResponsePartnerNewEmailOrAddress()) {
//                case ADDRESS -> reissueOption = isCourtService && emailPresent ? OFFLINE_AOS :
//                    emailPresent ? DIGITAL_AOS : REISSUE_CASE;
//
//                case EMAIL -> reissueOption =
//                    isCourtService ? DIGITAL_AOS : REISSUE_CASE;
//
//                case EMAIL_AND_ADDRESS -> reissueOption =
//                    isCourtService ? OFFLINE_AOS : REISSUE_CASE;
//
//                default -> reissueOption = REISSUE_CASE;
//            }
//        }

        // Option 2

//        if (noResponseOptions.getNoResponsePartnerNewEmailOrAddress() != null && !EMAIL.equals(noResponseOptions.getNoResponsePartnerNewEmailOrAddress())) {
//            caseTasks(setServiceType).run(caseDetails);
//        }

//        if (isCourtService && emailPresent) {
//            reissueOption = OFFLINE_AOS;
//        } else if (emailPresent) {
//            reissueOption = DIGITAL_AOS;
//        } else {
//            reissueOption = REISSUE_CASE;
//        }

        caseData.getApplication().setReissueOption(reissueOption);
    }
}
