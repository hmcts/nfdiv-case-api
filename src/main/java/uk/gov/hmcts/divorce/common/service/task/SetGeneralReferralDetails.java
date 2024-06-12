package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.CASEWORKER_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.PERMISSION_ON_DA_OOT;

@Component
@Slf4j
public class SetGeneralReferralDetails implements CaseTask {

    static final String FINAL_ORDER_OVERDUE = "Final order overdue";
    private static final String EXPEDITED_CASE = "Expedited case";

    static final String JUDGE_OR_LEGAL_ADVISOR_DETAILS =
        "Dear Judge please see the final order tab for the overdue reason from the applicant";

    static final String JUDGE_OR_LEGAL_ADVISOR_DETAILS_EXPEDITED =
            "Dear Judge this is an expedited case for your consideration";

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Executing the SetGeneralReferralDetails task. Case ID: {}", caseDetails.getId());

        GeneralReferral referralDetails;
        if (caseDetails.getState().equals(State.ExpeditedCase)) {
            referralDetails = GeneralReferral
                    .builder()
                    .generalReferralReason(CASEWORKER_REFERRAL)
                    .generalReferralUrgentCase(YES)
                    .generalReferralUrgentCaseReason(EXPEDITED_CASE)
                    .generalApplicationFrom(APPLICANT)
                    .generalApplicationReferralDate(now())
                    .generalReferralType(OTHER)
                    .generalReferralJudgeOrLegalAdvisorDetails(JUDGE_OR_LEGAL_ADVISOR_DETAILS_EXPEDITED)
                    .generalReferralFeeRequired(NO)
                    .build();

        } else {
            referralDetails = GeneralReferral
                    .builder()
                    .generalReferralReason(CASEWORKER_REFERRAL)
                    .generalReferralUrgentCase(YES)
                    .generalReferralUrgentCaseReason(FINAL_ORDER_OVERDUE)
                    .generalApplicationFrom(APPLICANT)
                    .generalApplicationReferralDate(now())
                    .generalReferralType(PERMISSION_ON_DA_OOT)
                    .generalReferralJudgeOrLegalAdvisorDetails(JUDGE_OR_LEGAL_ADVISOR_DETAILS)
                    .generalReferralFeeRequired(NO)
                    .build();
        }
        caseDetails.getData().setGeneralReferral(referralDetails);
        return caseDetails;
    }
}
