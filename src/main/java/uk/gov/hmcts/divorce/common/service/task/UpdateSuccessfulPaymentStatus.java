package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.List;

@Component
@Slf4j
public class UpdateSuccessfulPaymentStatus implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Setting payment status to Success for Case ID: {}", caseDetails.getId());

        List<ListValue<Payment>> activePayments = getPayments(caseDetails);
        if (CollectionUtils.isEmpty(activePayments)) {
            return caseDetails;
        }

        activePayments.getLast().getValue().setStatus(PaymentStatus.SUCCESS);

        return caseDetails;
    }

    private List<ListValue<Payment>> getPayments(final CaseDetails<CaseData, State> caseDetails) {
        final State state = caseDetails.getState();
        if (State.AwaitingPayment.equals(state)) {
            return caseDetails.getData().getApplication().getApplicationPayments();
        } else if (State.AwaitingFinalOrderPayment.equals(state)) {
            return caseDetails.getData().getFinalOrder().getFinalOrderPayments();
        } else if (State.AwaitingServicePayment.equals(state)) {
            return caseDetails.getData().getAlternativeService().getServicePayments();
        } else if (State.AwaitingGeneralApplicationPayment.equals(state)) {
            return caseDetails.getData().getApplicant1().getGeneralAppPayments();
        }

        return null;
    }
}
