package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.payment.model.CasePaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreateServiceRequestBody;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.ServiceReferenceResponse;
import uk.gov.hmcts.divorce.payment.model.StatusHistoriesItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.penceToPounds;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.CAE0001;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.CAE0003;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.CAE0004;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.GENERAL;
import static uk.gov.hmcts.divorce.payment.model.PbaErrorMessage.NOT_FOUND;

@Service
@Slf4j
public class PaymentService {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String ERROR_GENERIC = "Sorry, there is a problem with the service.\n"
        + "Try again later.";
    public static final String EVENT_ENFORCEMENT = "enforcement";
    public static final String EVENT_GENERAL = "general%20application";
    public static final String EVENT_ISSUE = "issue";
    public static final String EVENT_COPIES = "copies";
    public static final String SERVICE_DIVORCE = "divorce";
    public static final String SERVICE_OTHER = "other";
    public static final String KEYWORD_BAILIFF = "BailiffServeDoc";
    public static final String KEYWORD_DEEMED = "GeneralAppWithoutNotice";
    public static final String KEYWORD_DIVORCE = "DivorceCivPart";
    public static final String KEYWORD_DIVORCE_AMEND_PETITION = "DivorceAmendPetition";
    public static final String KEYWORD_DEF = "DEF";
    public static final String KEYWORD_NOTICE = "GAOnNotice";
    public static final String KEYWORD_WITHOUT_NOTICE = "GeneralAppWithoutNotice";
    public static final String KEYWORD_ABC = "ABC";

    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String DIVORCE_SERVICE = "DIVORCE";
    private static final String GBP = "GBP";
    public static final String CA_E0001 = "CA-E0001";
    public static final String CA_E0004 = "CA-E0004";
    public static final String CA_E0003 = "CA-E0003";
    public static final String HMCTS_ORG_ID = "ABA1";
    private static final String ERROR_SERVICE_REF_REQUEST = "Failed to create service reference for case: %s";

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private PaymentPbaClient paymentPbaClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    public static class PaymentServiceException extends RuntimeException {
        public PaymentServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String createServiceRequestReference(
        String callbackUrl,
        Long caseId,
        String responsibleParty,
        OrderSummary orderSummary
    ) {
        try {
            log.info("Creating service request reference for case id: {}", caseId);

            final Fee fee = getFeeValue(orderSummary);
            final PaymentItem paymentItem = PaymentItem
                .builder()
                .ccdCaseNumber(String.valueOf(caseId))
                .calculatedAmount(penceToPounds(orderSummary.getPaymentTotal()))
                .code(fee.getCode())
                .version(fee.getVersion())
                .build();

            var serviceReferenceResponse = paymentClient.createServiceRequest(
                httpServletRequest.getHeader(AUTHORIZATION),
                authTokenGenerator.generate(),
                buildServiceRequestBody(callbackUrl, caseId, responsibleParty, singletonList(paymentItem))
            );

            String serviceReference = Optional.ofNullable(serviceReferenceResponse)
                .map(response ->
                    Optional.ofNullable(response.getBody())
                        .map(ServiceReferenceResponse::getServiceRequestReference)
                        .filter(serviceRef -> !serviceRef.isEmpty())
                        .orElseThrow()
                )
                .orElseThrow();

            log.info("Successfully created service request reference: {}, for case id: {}", serviceReference, caseId);

            return serviceReference;
        } catch (FeignException | NoSuchElementException e) {
            log.error("Failed to create service request reference for case id: {}, error: {}", caseId, e.getMessage());

            throw new PaymentServiceException(String.format(ERROR_SERVICE_REF_REQUEST, caseId), e);
        }
    }

    public OrderSummary getOrderSummaryByServiceEvent(String service, String event, String keyword) {
        final var feeResponse = feesAndPaymentsClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            event,
            FAMILY,
            FAMILY_COURT,
            service,
            keyword
        );

        return OrderSummary
            .builder()
            .fees(singletonList(getFee(feeResponse)))
            .paymentTotal(getValueInPence(feeResponse.getAmount()))
            .build();
    }

    public PbaResponse processPbaPayment(CaseData caseData,
                                         Long caseId,
                                         Solicitor solicitor,
                                         String pbaNumber,
                                         OrderSummary orderSummary,
                                         String feeAccountReference) {

        log.info("Processing PBA payment for case id {}", caseId);

        ResponseEntity<CreditAccountPaymentResponse> paymentResponseEntity = null;

        try {
            paymentResponseEntity = paymentPbaClient.creditAccountPayment(
                httpServletRequest.getHeader(AUTHORIZATION),
                authTokenGenerator.generate(),
                creditAccountPaymentRequest(caseData, caseId, solicitor, pbaNumber, orderSummary, feeAccountReference)
            );

            String paymentReference = Optional.ofNullable(paymentResponseEntity)
                .map(response ->
                    Optional.ofNullable(response.getBody())
                        .map(CreditAccountPaymentResponse::getReference)
                        .orElseGet(() -> null)
                )
                .orElseGet(() -> null);

            log.info("For case id {} successfully processed PBA payment for account number {} and payment reference {}",
                caseId,
                pbaNumber,
                paymentReference
            );

            if (paymentResponseEntity != null) {
                return new PbaResponse(HttpStatus.resolve(paymentResponseEntity.getStatusCode().value()), null, paymentReference);
            }

        } catch (FeignException exception) {
            log.error("For case id {} unsuccessful payment for account number {} with exception {}",
                caseId,
                pbaNumber,
                exception.getMessage()
            );
            return getPbaErrorResponse(pbaNumber, exception);
        }
        return new PbaResponse(INTERNAL_SERVER_ERROR, ERROR_GENERIC, null);
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
            .orElseGet(() -> INTERNAL_SERVER_ERROR);

        if (httpStatus == HttpStatus.NOT_FOUND) {
            return new PbaResponse(httpStatus, String.format(NOT_FOUND.value(), pbaNumber), null);
        }

        if (isGenericErrorRequiredForHttpStatus(httpStatus)) {
            return new PbaResponse(httpStatus, ERROR_GENERIC, pbaNumber);
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
            switch (errorCode) {
                case CA_E0001:
                    log.info("Payment Reference: {} Generating error message for {} error code",
                        creditAccountPaymentResponse.getPaymentReference(),
                        errorCode
                    );
                    errorMessage = String.format(CAE0001.value(), pbaNumber);
                    break;
                case CA_E0004:
                    log.info("Payment Reference: {} Generating error message for {} error code",
                        creditAccountPaymentResponse.getPaymentReference(),
                        errorCode
                    );
                    errorMessage = String.format(CAE0004.value(), pbaNumber);
                    break;

                case CA_E0003:
                    log.info("Payment Reference: {} Generating error message for {} error code",
                        creditAccountPaymentResponse.getPaymentReference(),
                        errorCode
                    );
                    errorMessage = String.format(CAE0003.value(), pbaNumber);
                    break;

                default:
                    log.info("Payment Reference: {} Generating error message for {} error code",
                        creditAccountPaymentResponse.getPaymentReference(),
                        errorCode
                    );
                    errorMessage = GENERAL.value();
                    break;
            }
        } else {
            errorMessage = GENERAL.value();
        }
        return errorMessage;
    }

    private CreditAccountPaymentRequest creditAccountPaymentRequest(CaseData caseData,
                                                                    Long caseId,
                                                                    Solicitor solicitor,
                                                                    String pbaNumber,
                                                                    OrderSummary orderSummary,
                                                                    String feeAccountReference) {

        var creditAccountPaymentRequest = new CreditAccountPaymentRequest();
        creditAccountPaymentRequest.setService(DIVORCE_SERVICE);
        creditAccountPaymentRequest.setCurrency(GBP);
        creditAccountPaymentRequest.setAccountNumber(pbaNumber);
        creditAccountPaymentRequest.setCaseType(getCaseType());

        creditAccountPaymentRequest.setOrganisationName(solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());

        creditAccountPaymentRequest.setCustomerReference(feeAccountReference);

        final Fee fee = getFeeValue(orderSummary);
        creditAccountPaymentRequest.setDescription(fee.getDescription());

        creditAccountPaymentRequest.setAmount(orderSummary.getPaymentTotal());
        creditAccountPaymentRequest.setCcdCaseNumber(String.valueOf(caseId));

        List<PaymentItem> paymentItemList = populateFeesPaymentItems(
            caseId,
            orderSummary.getPaymentTotal(),
            fee,
            caseData.getApplication().getFeeAccountReference()
        );

        creditAccountPaymentRequest.setFees(paymentItemList);

        return creditAccountPaymentRequest;
    }

    private CreateServiceRequestBody buildServiceRequestBody(
        String callBackUrl,
        Long caseId,
        String responsibleParty,
        List<PaymentItem> paymentItemList
    ) {
        return CreateServiceRequestBody.builder()
            .ccdCaseNumber(caseId)
            .caseReference(caseId)
            .callBackUrl(callBackUrl)
            .hmctsOrgId(HMCTS_ORG_ID)
            .fees(paymentItemList)
            .casePaymentRequest(
                CasePaymentRequest.builder()
                    .responsibleParty(responsibleParty)
                    .action("payment")
                    .build()
            ).build();
    }

    private List<PaymentItem> populateFeesPaymentItems(
        Long caseId,
        String paymentTotal,
        Fee fee,
        String reference
    ) {
        var paymentItem = PaymentItem
            .builder()
            .ccdCaseNumber(String.valueOf(caseId))
            .calculatedAmount(penceToPounds(paymentTotal))
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

    public Double getServiceCost(String service, String event, String keyword) {

        final var feeResponse = feesAndPaymentsClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            event,
            FAMILY,
            FAMILY_COURT,
            service,
            keyword
        );

        return feeResponse.getAmount();
    }

    private boolean isGenericErrorRequiredForHttpStatus(HttpStatus httpStatus) {
        return httpStatus == INTERNAL_SERVER_ERROR || httpStatus == GATEWAY_TIMEOUT;
    }
}
