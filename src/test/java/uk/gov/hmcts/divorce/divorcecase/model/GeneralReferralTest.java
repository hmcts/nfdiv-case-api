package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMediumType.EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.CASEWORKER_REFERRAL;

class GeneralReferralTest {

    @Test
    void shouldCopyGeneralReferral() {

        final LocalDate applicationReferralDate = LocalDate.of(2022, 1, 26);
        final LocalDate generalApplicationAddedDate = LocalDate.of(2022, 1, 22);
        final LocalDate generalReferralDecisionDate = LocalDate.of(2022, 1, 27);
        final String generalReferralJudgeOrLegalAdvisorDetails = "details";
        final FeeDetails referralFee = FeeDetails.builder()
            .accountNumber("account")
            .build();
        final String generalReferralDecisionReason = "Reason";

        final GeneralReferral generalReferral = GeneralReferral.builder()
            .generalReferralReason(GENERAL_APPLICATION_REFERRAL)
            .generalApplicationFrom(APPLICANT)
            .generalApplicationReferralDate(applicationReferralDate)
            .generalApplicationAddedDate(generalApplicationAddedDate)
            .generalReferralType(CASEWORKER_REFERRAL)
            .alternativeServiceMedium(EMAIL)
            .generalReferralJudgeOrLegalAdvisorDetails(generalReferralJudgeOrLegalAdvisorDetails)
            .generalReferralFeeRequired(YES)
            .generalReferralFee(referralFee)
            .generalReferralDecision(APPROVE)
            .generalReferralDecisionDate(generalReferralDecisionDate)
            .generalReferralDecisionReason(generalReferralDecisionReason)
            .build();

        final GeneralReferral copy = generalReferral.copy();

        assertThat(copy).isNotSameAs(generalReferral);
        assertThat(copy).isEqualTo(generalReferral);
    }
}