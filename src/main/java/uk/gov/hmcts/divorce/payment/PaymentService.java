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
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.StatusHistoriesItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.CAE0001;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.CAE0004;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.GENERAL;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.NOT_FOUND;

@Service
@Slf4j
public class PaymentService {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String ISSUE_EVENT = "issue";
    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String DIVORCE = "divorce";
    private static final String GBP = "GBP";
    public static final String CA_E0001 = "CA-E0001";
    public static final String CA_E0004 = "CA-E0004";

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

    public PbaResponse processPbaPayment(CaseData caseData, Long caseId) {
        log.info("Processing PBA payment for case id {}", caseId);

        ResponseEntity<CreditAccountPaymentResponse> paymentResponseEntity = null;

        String pbaNumber = getPbaNumber(caseData);

        try {
            paymentResponseEntity = feesAndPaymentsClient.creditAccountPayment(
                httpServletRequest.getHeader(AUTHORIZATION),
                authTokenGenerator.generate(),
                creditAccountPaymentRequest(caseData, caseId)
            );

            String paymentReference = Optional.ofNullable(paymentResponseEntity)
                .map(response ->
                    Optional.ofNullable(response.getBody())
                        .map(CreditAccountPaymentResponse::getStatus)
                        .orElseGet(() -> null)
                )
                .orElseGet(() -> null);

            log.info("For case id {} successfully processed PBA payment for account number {}",
                caseId,
                getPbaNumber(caseData)
            );

            return new PbaResponse(paymentResponseEntity.getStatusCode(), null, paymentReference);

        } catch (FeignException exception) {
            log.error("For case id {} unsuccessful payment for account number {} with exception {}",
                caseId,
                pbaNumber,
                exception.getMessage()
            );
            return getPbaErrorResponse(pbaNumber, exception);
        }
    }

    private CreditAccountPaymentResponse getPaymentResponse(FeignException exception) {
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
        }

        return creditAccountPaymentResponse;
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

    private PbaResponse getPbaErrorResponse(String pbaNumber, FeignException exception) {
        HttpStatus httpStatus = Optional.ofNullable(HttpStatus.resolve(exception.status()))
            .orElseGet(() -> HttpStatus.INTERNAL_SERVER_ERROR);

        if (httpStatus == HttpStatus.NOT_FOUND) {
            return new PbaResponse(httpStatus, String.format(NOT_FOUND.value(), pbaNumber), null);
        }

        CreditAccountPaymentResponse creditAccountPaymentResponse = getPaymentResponse(exception);

        if (creditAccountPaymentResponse == null) {
            return new PbaResponse(httpStatus, GENERAL.value(), null);
        }

        List<StatusHistoriesItem> statusHistories = Objects.requireNonNull(creditAccountPaymentResponse).getStatusHistories();

        if (isEmpty(statusHistories)) {
            return new PbaResponse(httpStatus, GENERAL.value(), null);
        }

        StatusHistoriesItem statusHistoriesItem = statusHistories.get(0);

        String errorCode = statusHistoriesItem.getErrorCode();

        String errorMessage = populateErrorResponse(pbaNumber, creditAccountPaymentResponse, httpStatus, errorCode);

        return new PbaResponse(httpStatus, errorMessage, null);
    }

    private String populateErrorResponse(
        String pbaNumber,
        CreditAccountPaymentResponse creditAccountPaymentResponse,
        HttpStatus httpStatus,
        String errorCode
    ) {
        String errorMessage = null;
        if (httpStatus == HttpStatus.FORBIDDEN) {
            if (errorCode.equalsIgnoreCase(CA_E0001)) {
                log.info("Payment Reference: {} Generating error message for {} error code",
                    creditAccountPaymentResponse.getPaymentReference(),
                    CA_E0001
                );
                errorMessage = String.format(CAE0001.value(), pbaNumber);
            }
            if (errorCode.equalsIgnoreCase(CA_E0004)) {
                log.info("Payment Reference: {} Generating error message for {} error code",
                    creditAccountPaymentResponse.getPaymentReference(),
                    CA_E0004
                );
                errorMessage = String.format(CAE0004.value(), pbaNumber);
            }

        } else {
            errorMessage = GENERAL.value();
        }
        return errorMessage;
    }

    private CreditAccountPaymentRequest creditAccountPaymentRequest(CaseData caseData, Long caseId) {
        var creditAccountPaymentRequest = new CreditAccountPaymentRequest();
        var orderSummary = caseData.getApplication().getApplicationFeeOrderSummary();

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

        List<PaymentItem> paymentItemList =
            populateFeesPaymentItems(caseData, caseId, orderSummary.getPaymentTotal(), fee, solicitor.getReference());

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
