package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.service.task.SetGeneralReferralDetails.FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.common.service.task.SetGeneralReferralDetails.JUDGE_OR_LEGAL_ADVISOR_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.CASEWORKER_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.PERMISSION_ON_DA_OOT;

class SetGeneralReferralDetailsTest {

    @Test
    void shouldSetGeneralReferralDetails() {
        SetGeneralReferralDetails setGeneralReferralDetails = new SetGeneralReferralDetails();

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();


        final CaseDetails<CaseData, State> applied = setGeneralReferralDetails.apply(caseDetails);

        final GeneralReferral generalReferral = applied.getData().getGeneralReferral();

        assertAll(
            () -> assertEquals(CASEWORKER_REFERRAL, generalReferral.getGeneralReferralReason()),
            () -> assertEquals(YES, generalReferral.getGeneralReferralUrgentCase()),
            () -> assertEquals(FINAL_ORDER_OVERDUE, generalReferral.getGeneralReferralUrgentCaseReason()),
            () -> assertEquals(APPLICANT, generalReferral.getGeneralApplicationFrom()),
            () -> assertEquals(FINAL_ORDER_OVERDUE, generalReferral.getGeneralReferralUrgentCaseReason()),
            () -> assertEquals(now(), generalReferral.getGeneralApplicationReferralDate()),
            () -> assertEquals(PERMISSION_ON_DA_OOT, generalReferral.getGeneralReferralType()),
            () -> assertEquals(JUDGE_OR_LEGAL_ADVISOR_DETAILS, generalReferral.getGeneralReferralJudgeOrLegalAdvisorDetails()),
            () -> assertEquals(NO, generalReferral.getGeneralReferralFeeRequired())
        );
    }
}
