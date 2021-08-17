package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;
import uk.gov.hmcts.divorce.payment.model.PbaErrorMessage;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.StatusHistoriesItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;

@Service
@Slf4j
public class PaymentService {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String ISSUE_EVENT = "issue";
    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String DIVORCE = "divorce";
    public static final String GBP = "GBP";
    private static final String CAE0001 = "CA-E0001";
    private static final String CAE0004 = "CA-E0004";

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

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

    public PbaResponse processPbaPayment(CaseData caseData, Long caseId) {
        log.info("Processing PBA payment for case id {}", caseId);

        feesAndPaymentsClient.creditAccountPayment(
            httpServletRequest.getHeader(AUTHORIZATION),
            authTokenGenerator.generate(),
            creditAccountPaymentRequest(caseData, caseId)
        );

        ResponseEntity<CreditAccountPaymentResponse> paymentResponseResponseEntity = null;

        String pbaNumber = getPbaNumber(caseData);

        String errorMessage = null;
        try {
            paymentResponseResponseEntity = feesAndPaymentsClient.creditAccountPayment(
                httpServletRequest.getHeader(AUTHORIZATION),
                authTokenGenerator.generate(),
                creditAccountPaymentRequest(caseData, caseId)
            );
        } catch (FeignException exception) {
            log.error("For case id {} unsuccessful payment for account number {} with exception {}",
                caseId,
                getPbaNumber(caseData),
                exception.getMessage()
            );
            return getPbaErrorResponse(pbaNumber, exception);
        }

        log.info("For case id {} successfully processed PBA payment for account number {}",
            caseId,
            getPbaNumber(caseData)
        );

        return new PbaResponse(paymentResponseResponseEntity.getStatusCode(), null);
    }

    private PbaResponse getPbaErrorResponse(String pbaNumber, FeignException exception) {
        String errorMessage = null;
        HttpStatus httpStatus = Optional.ofNullable(HttpStatus.resolve(exception.status()))
            .orElseGet(() -> HttpStatus.INTERNAL_SERVER_ERROR);

        CreditAccountPaymentResponse creditAccountPaymentResponse = null;
        try {
            creditAccountPaymentResponse = objectMapper.readValue(
                exception.contentUTF8().getBytes(),
                CreditAccountPaymentResponse.class
            );
        } catch (IOException ioException) {
            log.warn("Could not convert error response to CreditAccountPaymentResponse object. Error message was {}",
                exception.contentUTF8()
            );
            errorMessage = PbaErrorMessage.GENERAL.value();
        }

        List<StatusHistoriesItem> statusHistories = creditAccountPaymentResponse.getStatusHistories();

        if (isEmpty(statusHistories)) {
            return new PbaResponse(httpStatus, PbaErrorMessage.GENERAL.value());
        }

        StatusHistoriesItem statusHistoriesItem = statusHistories.get(0);

        String errorCode = statusHistoriesItem.getErrorCode();

        if (httpStatus == HttpStatus.FORBIDDEN) {
            if (errorCode.equalsIgnoreCase(CAE0001)) {
                log.info("Payment Reference: {} Generating error message for {} error code",
                    creditAccountPaymentResponse.getPaymentReference(),
                    CAE0001
                );
                errorMessage = String.format(PbaErrorMessage.CAE0001.value(), pbaNumber);
            }
            if (errorCode.equalsIgnoreCase(CAE0004)) {
                log.info("Payment Reference: {} Generating error message for {} error code",
                    creditAccountPaymentResponse.getPaymentReference(),
                    CAE0004
                );
                errorMessage = String.format(PbaErrorMessage.CAE0004.value(), pbaNumber);
            }
        } else if (httpStatus == HttpStatus.NOT_FOUND) {
            errorMessage = String.format(PbaErrorMessage.NOT_FOUND.value(), pbaNumber);
        } else {
            errorMessage = PbaErrorMessage.GENERAL.value();
        }
        return new PbaResponse(httpStatus, errorMessage);
    }

    private CreditAccountPaymentRequest creditAccountPaymentRequest(CaseData caseData, Long caseId) {
        CreditAccountPaymentRequest creditAccountPaymentRequest = new CreditAccountPaymentRequest();
        OrderSummary orderSummary = caseData.getApplication().getApplicationFeeOrderSummary();

        creditAccountPaymentRequest.setService(DIVORCE);
        creditAccountPaymentRequest.setCurrency(GBP);
        creditAccountPaymentRequest.setSiteId(caseData.getSelectedDivorceCentreSiteId());
        creditAccountPaymentRequest.setAccountNumber(getPbaNumber(caseData));

        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();
        creditAccountPaymentRequest.setOrganisationName(solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());

        creditAccountPaymentRequest.setCustomerReference(solicitor.getReference());

        final Fee fee = getFeeValue(orderSummary);
        creditAccountPaymentRequest.setDescription(fee.getDescription());

        creditAccountPaymentRequest.setAmount(orderSummary.getPaymentTotal());
        creditAccountPaymentRequest.setCcdCaseNumber(String.valueOf(caseId));

        List<PaymentItem> paymentItemList = populateFeesPaymentItems(caseData, caseId, orderSummary.getPaymentTotal(), fee, solicitor.getReference());
        creditAccountPaymentRequest.setFees(paymentItemList);

        return creditAccountPaymentRequest;
    }

    private String getPbaNumber(CaseData caseData) {
        return caseData.getApplication().getPbaNumbers().getValue().getLabel();
    }

    private List<PaymentItem> populateFeesPaymentItems(
        CaseData caseData,
        Long caseId,
        String paymentTotal,
        Fee fee,
        String reference
    ) {
        var paymentItem = PaymentItem
            .builder()
            .ccdCaseNumber(String.valueOf(caseId))
            .calculatedAmount(paymentTotal)
            .code(fee.getCode())
            .reference(reference)
            .version(fee.getVersion())
            .build();


        return singletonList(paymentItem);
    }

    private Fee getFeeValue(OrderSummary orderSummary) {
        // We are always interested in the first fee. There may be a change in the future
        ListValue<Fee> feeItem = orderSummary.getFees().get(0);
        return feeItem.getValue();
    }
}
