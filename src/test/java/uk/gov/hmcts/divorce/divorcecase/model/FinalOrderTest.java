package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

class FinalOrderTest {

    @Test
    void shouldReturnDateUntilRespondentCanApplyForFinalOrder() {

        final FinalOrder finalOrder = FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.of(2021, 12, 7))
            .build();

        assertThat(finalOrder.calculateDateFinalOrderEligibleToRespondent())
            .isEqualTo(LocalDate.of(2022, 3, 7));
    }

    @Test
    void shouldReturnDateUntilCaseIsNoLongerEligibleForFinalOrder() {

        final LocalDate coPronouncementDate = LocalDate.of(2021, 12, 7);
        final FinalOrder finalOrder = FinalOrder.builder().build();

        assertThat(finalOrder.calculateDateFinalOrderNoLongerEligible(coPronouncementDate))
            .isEqualTo(LocalDate.of(2022, 12, 7));
    }

    @Test
    void shouldReturnDateFinalOrderEligibleFrom() {

        final LocalDateTime dateTime = LocalDateTime.of(2021, 12, 7, 10, 12, 0);
        final FinalOrder finalOrder = FinalOrder.builder().build();

        assertThat(finalOrder.getDateFinalOrderEligibleFrom(dateTime))
            .isEqualTo(LocalDate.of(2022, 1, 19));
    }

    @Test
    void shouldReturnTrueIfFinalOrderHasBeenSentToApplicant1() {

        final FinalOrder finalOrder = FinalOrder.builder()
            .finalOrderReminderSentApplicant1(YES)
            .build();

        assertThat(finalOrder.hasFinalOrderReminderSentApplicant1()).isTrue();
    }

    @Test
    void shouldReturnTrueIfApplicant1HasSubmittedFOButApplicant2HasNotSubmittedPast14Days() {

        var submittedDate = LocalDateTime.now().minusDays(15);

        final FinalOrder finalOrder  = FinalOrder.builder()
            .applicant1SubmittedFinalOrder(YES)
            .dateFinalOrderSubmitted(submittedDate).build();

        assertThat(finalOrder.shouldEnableSwitchToSoleFoForApplicant1()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1HasSubmittedFOButApplicant2HasNotSubmittedPast14Days() {

        var submittedDate = LocalDateTime.now();

        final FinalOrder finalOrder  = FinalOrder.builder()
            .applicant1SubmittedFinalOrder(YES)
            .dateFinalOrderSubmitted(submittedDate).build();

        assertThat(finalOrder.shouldEnableSwitchToSoleFoForApplicant1()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant2HasSubmittedFOButApplicant1HasNotSubmittedPast14Days() {

        var submittedDate = LocalDateTime.now().minusDays(15);

        final FinalOrder finalOrder  = FinalOrder.builder()
            .applicant2SubmittedFinalOrder(YES)
            .dateFinalOrderSubmitted(submittedDate).build();

        assertThat(finalOrder.shouldEnableSwitchToSoleFoForApplicant2()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant2HasSubmittedFOButApplicant2HasNotSubmittedPast14Days() {

        var submittedDate = LocalDateTime.now();

        final FinalOrder finalOrder  = FinalOrder.builder()
            .applicant1SubmittedFinalOrder(YES)
            .dateFinalOrderSubmitted(submittedDate).build();

        assertThat(finalOrder.shouldEnableSwitchToSoleFoForApplicant2()).isFalse();
    }
}
