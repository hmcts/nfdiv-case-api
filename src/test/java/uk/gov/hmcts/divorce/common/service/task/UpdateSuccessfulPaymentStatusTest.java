package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class UpdateSuccessfulPaymentStatusTest {

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails;

    @InjectMocks
    private UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    @BeforeEach
    void setUp() {

        final Payment payment = Payment
                .builder()
                .status(PaymentStatus.IN_PROGRESS)
                .reference(UUID.randomUUID().toString())
                .build();
        final ListValue<Payment> paymentListValue = ListValue
                .<Payment>builder()
                .value(payment)
                .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);

        caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        caseDetails.setData(CaseData.builder().application(
                        Application.builder().applicationPayments(payments).build())
                .build());

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(AwaitingPayment);
    }

    @Test
    void shouldSetDateSubmittedIfStateIsSubmitted() {

        final CaseDetails<CaseData, State> result = updateSuccessfulPaymentStatus.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingPayment);
        final CaseData resultData = result.getData();
        assertThat(resultData.getApplication().getApplicationPayments().get(0).getValue().getStatus()).isEqualTo(SUCCESS);
    }
}
