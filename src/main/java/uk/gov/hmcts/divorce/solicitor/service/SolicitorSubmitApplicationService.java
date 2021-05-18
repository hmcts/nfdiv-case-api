package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Service
@Slf4j
public class SolicitorSubmitApplicationService {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String ISSUE_EVENT = "issue";
    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String DIVORCE = "divorce";

    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Autowired
    private Clock clock;

    public OrderSummary getOrderSummary() {
        final var feeResponse = feesAndPaymentsClient.getApplicationIssueFee(
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

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseData caseData,
        final Long caseId,
        final String userAuth
    ) {

        State state = SolicitorAwaitingPaymentConfirmation;

        List<String> submittedErrors = Submitted.validate(caseData);

        if (submittedErrors.isEmpty()) {
            caseData.setDateSubmitted(LocalDateTime.now(clock));
            state = Submitted;
        }

        final var caseDataUpdaters = asList(
            miniApplicationRemover,
            solicitorSubmitNotification
        );

        final var caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .userAuthToken(userAuth)
            .build();

        final var updatedCaseData = caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseData)
            .state(state)
            .errors(submittedErrors)
            .build();
    }

    private ListValue<Fee> getFee(final FeeResponse feeResponse) {
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

    public Payment getDummyPayment(final OrderSummary orderSummary) {
        return Payment
            .builder()
            .paymentAmount(Integer.parseInt(orderSummary.getPaymentTotal()))
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference(orderSummary.getPaymentReference())
            .paymentSiteId("AA04")
            .paymentStatus(SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build();
    }
}
