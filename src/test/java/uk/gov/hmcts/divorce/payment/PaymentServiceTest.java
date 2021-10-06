package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
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
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.StatusHistoriesItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static feign.Request.HttpMethod.GET;
import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0001;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0003;
import static uk.gov.hmcts.divorce.payment.PaymentService.CA_E0004;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Mock
    private PaymentPbaClient paymentPbaClient;

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
            .getApplicationIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        OrderSummary orderSummary = paymentService.getOrderSummary();
        assertThat(orderSummary.getPaymentReference()).isNull();
        assertThat(orderSummary.getPaymentTotal()).isEqualTo(String.valueOf(1000));// in pence
        assertThat(orderSummary.getFees())
            .extracting("value", Fee.class)
            .extracting("description", "version", "code", "amount")
            .contains(tuple(ISSUE_FEE, "1", FEE_CODE, "10.0")
            );

        verify(feesAndPaymentsClient)
            .getApplicationIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
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
            .getApplicationIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        assertThatThrownBy(() -> paymentService.getOrderSummary())
            .hasMessageContaining("404 Fee Not found")
            .isExactlyInstanceOf(FeignException.NotFound.class);
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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment Account PBA0012345 is on hold. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturn4InternalServerErrorWhenResponseEntityIsNull() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentPbaClient.creditAccountPayment(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CreditAccountPaymentRequest.class)
        )).thenReturn(null);

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

        assertThat(response.getHttpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment request failed. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo(
                "Payment request failed. "
                    + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                    + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    @Test
    public void shouldReturn404WhenPaymentAccountIsNotFound() throws Exception {
        var caseData = caseData();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        Request request = Request.create(POST, EMPTY, Map.of(), null, UTF_8, null);

        byte[] body = "Account information could not be found".getBytes(UTF_8);
        FeignException feignException = new FeignException.FeignClientException(NOT_FOUND.value(), "error", request, body);

        doThrow(feignException)
            .when(paymentPbaClient).creditAccountPayment(
                eq(TEST_AUTHORIZATION_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CreditAccountPaymentRequest.class)
            );

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

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

        PbaResponse response = paymentService.processPbaPayment(caseData, TEST_CASE_ID, solicitor());

        assertThat(response.getHttpStatus()).isEqualTo(FORBIDDEN);
        assertThat(response.getErrorMessage())
            .isEqualTo("Payment request failed. "
                + "Please try again after 2 minutes with a different Payment Account, or alternatively use a different payment method. "
                + "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."
            );
    }

    private CaseData caseData() {
        var caseData = caseDataWithOrderSummary();
        caseData.setSelectedDivorceCentreSiteId("test_site_id");
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

        return new FeignException.FeignClientException(HttpStatus.FORBIDDEN.value(), "error", request, body);
    }

    private Solicitor solicitor() {
        return Solicitor
            .builder()
            .organisationPolicy(organisationPolicy())
            .reference("testref")
            .build();
    }
}
