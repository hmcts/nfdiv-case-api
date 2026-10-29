package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.payment.rule.PaymentMadeRule;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UpdateSuccessfulPaymentStatus implements CaseTask {

    private final PaymentMadeRule paymentMadeRule;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Setting payment status to Success for Case ID: {}", caseDetails.getId());

        List<ListValue<Payment>> activePayments = paymentMadeRule.getPayments(caseDetails.getData());
        if (CollectionUtils.isEmpty(activePayments)) {
            return caseDetails;
        }

        activePayments.getLast().getValue().setStatus(PaymentStatus.SUCCESS);

        return caseDetails;
    }
}
