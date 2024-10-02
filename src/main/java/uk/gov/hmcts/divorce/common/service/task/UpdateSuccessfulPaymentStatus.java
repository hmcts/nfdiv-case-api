package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;

@Component
@Slf4j
public class UpdateSuccessfulPaymentStatus implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Setting payment status to Success for Case ID: {}", caseDetails.getId());

        if (caseDetails.getState() == AwaitingPayment) {
            List<ListValue<Payment>> applicationPayment =
                    caseDetails.getData().getApplication().getApplicationPayments();

            caseDetails.getData().getApplication().getApplicationPayments().get(applicationPayment.size() - 1)
                    .getValue().setStatus(PaymentStatus.SUCCESS);
        } else {
            List<ListValue<Payment>> finalOrderPayment =
                    caseDetails.getData().getFinalOrder().getFinalOrderPayments();

            caseDetails.getData().getFinalOrder().getFinalOrderPayments().get(finalOrderPayment.size() - 1)
                    .getValue().setStatus(PaymentStatus.SUCCESS);
        }

        return caseDetails;
    }
}
