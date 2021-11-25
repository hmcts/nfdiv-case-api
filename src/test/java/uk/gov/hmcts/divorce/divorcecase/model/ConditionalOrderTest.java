package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

class ConditionalOrderTest {

    @Test
    void shouldReturnTrueIf() {

        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .claimsGranted(YES)
            .build();

        assertThat(conditionalOrder.areClaimsGranted()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotAppliedForFinancialOrder() {

        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .claimsGranted(NO)
            .build();

        assertThat(conditionalOrder.areClaimsGranted()).isFalse();
    }

    @Test
    void shouldReturnFalseIfAppliedForFinancialOrderIsSetToNull() {

        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .build();

        assertThat(conditionalOrder.areClaimsGranted()).isFalse();
    }
}