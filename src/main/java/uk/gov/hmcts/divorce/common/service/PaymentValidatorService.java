package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;

@Slf4j
@Service
public class PaymentValidatorService {

    public static final String ERROR_PAYMENT_IN_PROGRESS = "Payment in progress";

    public static final String ERROR_PAYMENT_INCOMPLETE = "Payment incomplete";

    public List<String> validatePayments(final List<ListValue<Payment>> payments, Long caseId) {
        List<String> validationErrors = new ArrayList<>();

        PaymentStatus lastPaymentStatus = lastPaymentStatus(payments);

        if (IN_PROGRESS.equals(lastPaymentStatus)) {
            log.info("Case {} payment in progress", caseId);

            return Collections.singletonList(ERROR_PAYMENT_IN_PROGRESS);
        } else if (!SUCCESS.equals(lastPaymentStatus)) {
            log.info("Case {} payment incomplete", caseId);

            return Collections.singletonList(ERROR_PAYMENT_INCOMPLETE);
        }

        return validationErrors;
    }

    private PaymentStatus lastPaymentStatus(List<ListValue<Payment>> payments) {
        return CollectionUtils.isEmpty(payments)
            ? null : payments.get(payments.size() - 1).getValue().getStatus();
    }
}

