package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_IN_PROGRESS;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.CANCELLED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class PaymentValidatorServiceTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE =
        "Statement of truth must be accepted by the person making the application";

    @InjectMocks
    private PaymentValidatorService paymentValidatorService;

    @Test
    void givenLastPaymentInProgressThenReturnValidationError() {
        Payment payment = Payment.builder().amount(55000).status(IN_PROGRESS).build();
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", payment));

        List<String> errors = paymentValidatorService.validatePayments(payments, TEST_CASE_ID);

        assertThat(errors).isEqualTo(singletonList(ERROR_PAYMENT_IN_PROGRESS));
    }

    @Test
    void givenLastPaymentCancelledThenReturnValidationError() {
        Payment payment = Payment.builder().amount(55000).status(CANCELLED).build();
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", payment));

        List<String> errors = paymentValidatorService.validatePayments(payments, TEST_CASE_ID);

        assertThat(errors).isEqualTo(singletonList(ERROR_PAYMENT_INCOMPLETE));
    }

    @Test
    void givenLastPaymentDeclineThenReturnValidationError() {
        Payment payment = Payment.builder().amount(55000).status(DECLINED).build();
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", payment));

        List<String> errors = paymentValidatorService.validatePayments(payments, TEST_CASE_ID);

        assertThat(errors).isEqualTo(singletonList(ERROR_PAYMENT_INCOMPLETE));
    }

    @Test
    void givenLastPaymentSuccessfulThenDontReturnValidationError() {
        Payment payment = Payment.builder().amount(55000).status(SUCCESS).build();
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", payment));

        List<String> errors = paymentValidatorService.validatePayments(payments, TEST_CASE_ID);

        assertThat(errors).isEqualTo(emptyList());
    }
}
