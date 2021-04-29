package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.solicitor.service.notification.ApplicantSubmittedNotification;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;

@Service
@Slf4j
public class SolicitorSubmitPetitionService {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String ISSUE_EVENT = "issue";
    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String DIVORCE = "divorce";

    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Autowired
    private ApplicantSubmittedNotification applicantSubmittedNotification;

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

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details) {

        applicantSubmittedNotification.send(details.getData(), details.getId());
        details.setState(SolicitorAwaitingPaymentConfirmation);

        return details;
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
