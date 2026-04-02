package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.task.SetGeneralReferralDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferral.CASEWORKER_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralService {

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdUpdateService ccdUpdateService;

    private final SetGeneralReferralDetails setGeneralReferralDetails;

    private final Clock clock;

    public void caseWorkerGeneralReferral(final CaseDetails<CaseData, State> details) {
        if (finalOrderRequestedAndOverdue(details)) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();
            final String caseId = details.getId().toString();

            log.info("CaseID {} FO Requested and Overdue.  Triggering Caseworker general referral event.", details.getId());

            ccdUpdateService
                .submitEventWithRetry(caseId, CASEWORKER_GENERAL_REFERRAL, setGeneralReferralDetails, user, serviceAuthorization);
        } else {
            log.info("CaseID {} Does not meet Requested & Overdue status.  Skipping general referral event.", details.getId());
        }
    }

    public GeneralReferral buildGeneralReferral(GeneralApplication generalApplication) {
        return GeneralReferral.builder()
            .generalReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL)
            .generalReferralFraudCase(YesOrNo.NO)
            .generalReferralUrgentCase(YesOrNo.NO)
            .generalApplicationFrom(generalApplication.getGeneralApplicationParty())
            .generalApplicationReferralDate(LocalDate.now(clock))
            .generalApplicationAddedDate(generalApplication.getGeneralApplicationReceivedDate().toLocalDate())
            .generalReferralType(GeneralReferralType.DISCLOSURE_VIA_DWP)
            .generalReferralFee(generalApplication.getGeneralApplicationFee())
            .generalReferralJudgeOrLegalAdvisorDetails(
                "Please refer to the Search Government Records application in the general applications tab"
            )
            .build();
    }

    private boolean finalOrderRequestedAndOverdue(final CaseDetails<CaseData, State> details) {
        final State state = details.getState();
        final boolean requestedState = FinalOrderRequested.equals(state) || RespondentFinalOrderRequested.equals(state);
        final boolean finalOrderOverdue = YesOrNo.YES.equals(details.getData().getFinalOrder().getIsFinalOrderOverdue());
        log.info("CaseID {} Final Order Requested: {}", details.getId(), requestedState);
        log.info("CaseID {} Final Order Overdue: {}", details.getId(), finalOrderOverdue);
        return requestedState && finalOrderOverdue;
    }
}
