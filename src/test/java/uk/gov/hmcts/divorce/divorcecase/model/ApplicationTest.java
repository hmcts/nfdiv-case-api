package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;

class ApplicationTest {

    @Test
    void shouldReturnTrueIfApplicant1HelpWithFeesNeedHelpIsSetToYes() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnTrueIfSolPaymentHowToPayIsSetToHelpWithFees() {

        final Application application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .build();

        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsSetToNo() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(NO)
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsNull() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesIsNull() {

        final Application application = Application.builder().build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueIfSolicitorApplication() {
        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        assertThat(application.isSolicitorApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotSolicitorApplication() {

        assertThat(Application.builder().solSignStatementOfTruth(NO).build()
            .isSolicitorApplication()).isFalse();

        assertThat(Application.builder().build()
            .isSolicitorApplication()).isFalse();
    }
}