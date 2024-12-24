package uk.gov.hmcts.divorce.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.payment.model.callback.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.PaymentCallbackService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;

@ExtendWith(MockitoExtension.class)
public class PaymentCallbackControllerTest {

    @Mock
    private PaymentCallbackService paymentCallbackService;

    @InjectMocks
    private PaymentCallbackController paymentCallbackController;

    @Test
    void shouldProcessCallbackByDelegatingToPaymentCallbackService() {
        PaymentCallbackDto paymentCallbackDto = PaymentCallbackDto.builder().build();

        paymentCallbackController.updatePayment(TEST_AUTHORIZATION_TOKEN, paymentCallbackDto);

        verify(paymentCallbackService).handleCallback(paymentCallbackDto);
    }
}
