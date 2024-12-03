package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.payment.model.CreateServiceRequestBody;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.ServiceReferenceResponse;
import uk.gov.hmcts.divorce.payment.model.StatusHistoriesItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0001;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0003;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0004;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.orderSummaryWithFee;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String FAMILY = "family";
    private static final String FAMILY_COURT = "family court";
    private static final String KEYWORD_INVALID = "invalid-keyword";
    private static final String PBA_NUMBER = "PBA0012345";
    private static final String FEE_ACCOUNT_REF = "REF01";

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Mock
    private PaymentPbaClient paymentPbaClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResponseEntity<CreditAccountPaymentResponse> responseEntity;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void shouldReturnOrderSummaryWhenFeeEventIsAvailable() {
        doReturn(getFeeResponse())
            .when(feesAndPaymentsClient)
            .getPaymentServiceFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
        assertThat(orderSummary.getPaymentReference()).isNull();
        assertThat(orderSummary.getPaymentTotal()).isEqualTo(String.valueOf(1000));// in pence
        assertThat(orderSummary.getFees())
            .extracting("value", Fee.class)
            .extracting("description", "version", "code", "amount")
            .contains(tuple(ISSUE_FEE, "1", FEE_CODE, "1000")
            );

        verify(feesAndPaymentsClient)
            .getPaymentServiceFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        verifyNoMoreInteractions(feesAndPaymentsClient);
    }

    @Test
    public void shouldReturnOrderSummaryForServiceEventKeyword() {
        doReturn(getFeeResponse())
            .when(feesAndPaymentsClient)
            .getPaymentServiceFee(
                DEFAULT_CHANNEL,
                EVENT_ENFORCEMENT,
                FAMILY,
                FAMILY_COURT,
                SERVICE_OTHER,
                KEYWORD_BAILIFF
            );

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF);
        assertThat(orderSummary.getPaymentReference()).isNull();
        assertThat(orderSummary.getPaymentTotal()).isEqualTo(String.valueOf(1000));// in pence
        assertThat(orderSummary.getFees())
            .extracting("value", Fee.class)
            .extracting("description", "version", "code", "amount")
            .contains(tuple(ISSUE_FEE, "1", FEE_CODE, "1000")
            );

        verify(feesAndPaymentsClient)
            .getPaymentServiceFee(
                DEFAULT_CHANNEL,
                EVENT_ENFORCEMENT,
                FAMILY,
                FAMILY_COURT,
                SERVICE_OTHER,
                KEYWORD_BAILIFF
            );

        verifyNoMoreInteractions(feesAndPaymentsClient);
    }

    @Test
    public void shouldThrowFeignExceptionWhenFeeEventIsNotAvailable() {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "feeLookupNotFound",
            Response.builder()
                .request(request)
                .status(404)
                .headers(Collections.emptyMap())
                .reason("Fee Not found")
                .build()
        );

        doThrow(feignException)
            .when(feesAndPaymentsClient)
            .getPaymentServiceFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        assertThatThrownBy(() -> paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_INVALID))
            .hasMessageContaining("404 Fee Not found")
            .isExactlyInstanceOf(FeignException.NotFound.class);
    }

    @Test
    public void shouldSuccessfullyCreateServiceRequests() {
        var serviceRefResponse = ServiceReferenceResponse.builder().serviceRequestReference(TEST_SERVICE_REFERENCE).build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentClient.createServiceRequest(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreateServiceRequestBody.class)
            )
        ).thenReturn(ResponseEntity.ok().body(serviceRefResponse));

        String result = paymentService.createServiceRequestReference(
            "payment-callback", TEST_CASE_ID, "respondent", orderSummaryWithFee()
        );

        assertThat(result).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    public void shouldProcessPbaPaymentSuccessfullyWhenPbaAccountIsValid() {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentPbaClient.creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCode()).thenReturn(CREATED);

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getHttpStatus()).isEqualTo(CREATED);

        verify(httpServletRequest).getHeader(AUTHORIZATION);
        verify(authTokenGenerator).generate();
        verify(paymentPbaClient).creditAccountPayment(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CreditAccountPaymentRequest.class)
        );
    }

    @Test
    public void shouldReturn403WithErrorCodeCae0004WhenAccountIsDeleted() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse = buildPaymentClientResponse(CA_E0004, "Your account is deleted");

        FeignException feignException = feignException(creditAccountPaymentResponse);

        when(objectMapper.readValue(
            feignException.contentUTF8().getBytes(),
            CreditAccountPaymentResponse.class
        )).thenReturn(creditAccountPaymentResponse);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment Account PBA0012345 has been deleted. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturn403WithErrorCodeCae0003WhenAccountIsHold() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse = buildPaymentClientResponse(CA_E0003, "Your account is on hold");

        FeignException feignException = feignException(creditAccountPaymentResponse);

        when(objectMapper.readValue(
            feignException.contentUTF8().getBytes(),
            CreditAccountPaymentResponse.class
        )).thenReturn(creditAccountPaymentResponse);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment Account PBA0012345 is on hold. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturn4InternalServerErrorWhenResponseEntityIsNull() {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentPbaClient.creditAccountPayment(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CreditAccountPaymentRequest.class)
        )).thenReturn(null);

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Sorry, there is a problem with the service.\n"
                    + "Try again later."
            );
    }

    @Test
    public void shouldReturnGenericErrorWhenGatewayTimeout() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse = buildPaymentClientResponse(GATEWAY_TIMEOUT.toString(), "Error");
        byte[] body = new ObjectMapper().writeValueAsString(creditAccountPaymentResponse).getBytes();
        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);
        FeignException feignException = new FeignException.GatewayTimeout(GATEWAY_TIMEOUT.toString(),request, body, emptyMap());

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(GATEWAY_TIMEOUT);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Sorry, there is a problem with the service.\n"
                    + "Try again later."
            );

    }

    @Test
    public void shouldReturnGeneralErrorWhenOtherHttpError() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse = buildPaymentClientResponse(SERVICE_UNAVAILABLE.toString(), "Error");
        byte[] body = new ObjectMapper().writeValueAsString(creditAccountPaymentResponse).getBytes();
        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);
        FeignException feignException = new FeignException.ServiceUnavailable(SERVICE_UNAVAILABLE.toString(),request, body, emptyMap());

        when(objectMapper.readValue(
            feignException.contentUTF8().getBytes(),
            CreditAccountPaymentResponse.class
        )).thenReturn(creditAccountPaymentResponse);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(SERVICE_UNAVAILABLE);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment request failed. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );

    }

    @Test
    public void shouldReturnGenericErrorWhenInternalServerError() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse = buildPaymentClientResponse(INTERNAL_SERVER_ERROR.toString(), "Error");
        byte[] body = new ObjectMapper().writeValueAsString(creditAccountPaymentResponse).getBytes();
        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);
        FeignException feignException = new FeignException.InternalServerError(INTERNAL_SERVER_ERROR.toString(),request, body, emptyMap());

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Sorry, there is a problem with the service.\n"
                    + "Try again later."
            );

    }

    @Test
    public void shouldReturn403WithErrorCodeCae0001WhenAccountHasInsufficientBalance() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse =
            buildPaymentClientResponse(CA_E0001, "Fee account has insufficient funds available");

        FeignException feignException = feignException(creditAccountPaymentResponse);

        when(objectMapper.readValue(
            feignException.contentUTF8().getBytes(),
            CreditAccountPaymentResponse.class
        )).thenReturn(creditAccountPaymentResponse);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Fee account PBA0012345 has insufficient funds available. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturnGeneralErrorWhenErrorCodeIsUnknown() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CreditAccountPaymentResponse creditAccountPaymentResponse =
            buildPaymentClientResponse("error_code", "Some error");

        FeignException feignException = feignException(creditAccountPaymentResponse);

        when(objectMapper.readValue(
            feignException.contentUTF8().getBytes(),
            CreditAccountPaymentResponse.class
        )).thenReturn(creditAccountPaymentResponse);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment request failed. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturn404WhenPaymentAccountIsNotFound() {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);

        byte[] body = "Account information could not be found".getBytes(UTF_8);
        FeignException feignException = new FeignException.FeignClientException(NOT_FOUND.value(), "error", request, body, emptyMap());

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(NOT_FOUND);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment Account PBA0012345 cannot be found. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );

    }

    @Test
    public void shouldReturnGeneralErrorWhenThereIsAnErrorParsingPaymentResponse() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);


        FeignException feignException = feignException(null);

        doThrow(new IOException("error parsing response"))
            .when(objectMapper).readValue(
                feignException.contentUTF8().getBytes(),
                CreditAccountPaymentResponse.class
            );

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(
            caseData, TEST_CASE_ID, solicitor(), PBA_NUMBER, orderSummaryWithFee(), FEE_ACCOUNT_REF);

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo("Payment request failed. "
                + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    private CaseData caseData() {
        var caseData = caseDataWithOrderSummary();
        caseData.getApplication().setPbaNumbers(getPbaNumbersForAccount("PBA0012345"));
        caseData.getApplicant1().setSolicitor(
            Solicitor
                .builder()
                .reference("1234")
                .organisationPolicy(organisationPolicy())
                .build()
        );
        return caseData;
    }

    private static CreditAccountPaymentResponse buildPaymentClientResponse(
        String errorCode,
        String errorMessage
    ) {
        return CreditAccountPaymentResponse.builder()
            .dateCreated("2021-08-18T10:22:33.449+0000")
            .status("Failed")
            .paymentGroupReference("2020-1601893353478")
            .statusHistories(
                singletonList(
                    StatusHistoriesItem.builder()
                        .status("Failed")
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .dateCreated("2021-08-18T10:22:33.449+0000")
                        .dateUpdated("2021-08-18T10:22:33.449+0000")
                        .build()
                )
            )
            .build();
    }

    private FeignException feignException(CreditAccountPaymentResponse creditAccountPaymentResponse) throws JsonProcessingException {
        byte[] body = new ObjectMapper().writeValueAsString(creditAccountPaymentResponse).getBytes();
        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);

        return new FeignException.FeignClientException(HttpStatus.FORBIDDEN.value(), "error", request, body, emptyMap());
    }

    private Solicitor solicitor() {
        return Solicitor
            .builder()
            .organisationPolicy(organisationPolicy())
            .reference(TEST_REFERENCE)
            .build();
    }

    @Test
    public void getServiceCostShouldReturnFeeAmountWhenFeeEventIsAvailable() {
        FeeResponse feeResponse = getFeeResponse();

        doReturn(feeResponse)
            .when(feesAndPaymentsClient)
            .getPaymentServiceFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        Assertions.assertEquals(10.0,
            paymentService.getServiceCost(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF));

        verify(feesAndPaymentsClient)
            .getPaymentServiceFee(
                DEFAULT_CHANNEL,
                EVENT_ENFORCEMENT,
                FAMILY,
                FAMILY_COURT,
                SERVICE_OTHER,
                KEYWORD_BAILIFF
            );
    }

    @Test
    public void getServiceCostShouldThrowFeignExceptionWhenFeeEventIsNotAvailable() {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "feeLookupNotFound",
            Response.builder()
                .request(request)
                .status(404)
                .headers(Collections.emptyMap())
                .reason("Fee Not found")
                .build()
        );

        doThrow(feignException)
            .when(feesAndPaymentsClient)
            .getPaymentServiceFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        assertThatThrownBy(() -> paymentService.getServiceCost(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_INVALID))
            .hasMessageContaining("404 Fee Not found")
            .isExactlyInstanceOf(FeignException.NotFound.class);
    }
}
