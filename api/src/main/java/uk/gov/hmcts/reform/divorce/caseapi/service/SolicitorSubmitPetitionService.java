package uk.gov.hmcts.reform.divorce.caseapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.DEFAULT_CHANNEL;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.DIVORCE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FAMILY;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FAMILY_COURT;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.ISSUE_EVENT;

@Service
@Slf4j
public class SolicitorSubmitPetitionService {
    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    public OrderSummary getOrderSummary() {
        FeeResponse feeResponse = feesAndPaymentsClient.getPetitionIssueFee(
            DEFAULT_CHANNEL,
            ISSUE_EVENT,
            FAMILY,
            FAMILY_COURT,
            DIVORCE,
            null
        );

        return OrderSummary
            .builder()
            .fees(singletonList(getFee(feeResponse)))
            .paymentTotal(getValueInPence(feeResponse.getAmount()))
            .build();
    }

    private ListValue<Fee> getFee(FeeResponse feeResponse) {
        return ListValue
            .<Fee>builder()
            .value(
                Fee
                    .builder()
                    .amount(getValueInPence(feeResponse.getAmount()))
                    .code(feeResponse.getFeeCode())
                    .description(feeResponse.getDescription())
                    .version(String.valueOf(feeResponse.getVersion()))
                    .build()
            )
            .build();
    }
}
