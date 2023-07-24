package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void shouldReturnTrueIfApplicant1FinalOrderLateExplanation() {
        final FinalOrder finalOrder = FinalOrder.builder()
            .applicant1FinalOrderLateExplanation("Some explanation")
            .build();
        assertTrue(finalOrder.hasFinalOrderLateExplanation());
    }

    @Test
    void shouldReturnTrueIfApplicant1FinalOrderLateExplanationTranslated() {
        final FinalOrder finalOrder = FinalOrder.builder()
            .applicant1FinalOrderLateExplanationTranslated("Some explanation")
            .build();
        assertTrue(finalOrder.hasFinalOrderLateExplanation());
    }

    @Test
    void shouldReturnTrueIfApplicant2FinalOrderLateExplanation() {
        final FinalOrder finalOrder = FinalOrder.builder()
            .applicant2FinalOrderLateExplanation("Some explanation")
            .build();
        assertTrue(finalOrder.hasFinalOrderLateExplanation());
    }

    @Test
    void shouldReturnTrueIfApplicant2FinalOrderLateExplanationTranslated() {
        final FinalOrder finalOrder = FinalOrder.builder()
            .applicant2FinalOrderLateExplanationTranslated("Some explanation")
            .build();
        assertTrue(finalOrder.hasFinalOrderLateExplanation());
    }

    @Test
    void shouldReturnFalseIfNoFinalOrderLateExplanation() {
        final FinalOrder finalOrder = FinalOrder.builder()
            .build();
        assertFalse(finalOrder.hasFinalOrderLateExplanation());
    }
}
