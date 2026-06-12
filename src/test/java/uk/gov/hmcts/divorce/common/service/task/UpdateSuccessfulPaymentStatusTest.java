package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.rule.PaymentMadeRule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class UpdateSuccessfulPaymentStatusTest {
    @Mock
    private PaymentMadeRule paymentMadeRule;

    @InjectMocks
    private UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    @Test
    void shouldSetPaymentStatusToSuccess() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        Application application = Application.builder()
            .applicationPayments(buildPaymentsInProgress())
            .build();
        caseData.setApplication(application);

        when(paymentMadeRule.getPayments(caseData)).thenReturn(application.getApplicationPayments());

        CaseDetails<CaseData, State> result = updateSuccessfulPaymentStatus.apply(caseDetails);

        PaymentStatus updatedPaymentStatus = result.getData().getApplication().getApplicationPayments()
            .getLast().getValue().getStatus();

        assertThat(updatedPaymentStatus).isEqualTo(PaymentStatus.SUCCESS);
    }

    private List<ListValue<Payment>> buildPaymentsInProgress() {
        return getPayments(Payment
            .builder()
            .status(PaymentStatus.IN_PROGRESS)
            .reference(UUID.randomUUID().toString())
            .build());
    }

    private List<ListValue<Payment>> getPayments(Payment payment) {
        final ListValue<Payment> paymentListValue = ListValue
                .<Payment>builder()
                .value(payment)
                .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);

        return payments;
    }
}
