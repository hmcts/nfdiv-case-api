package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.orderSummaryWithFee;

public final class PaymentWireMock {

    private static final WireMockServer PAYMENTS_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PaymentWireMock() {
    }

    public static void start() {
        if (!PAYMENTS_SERVER.isRunning()) {
            PAYMENTS_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (PAYMENTS_SERVER.isRunning()) {
            PAYMENTS_SERVER.stop();
            PAYMENTS_SERVER.resetAll();
        }
    }

    public static void stubCreditAccountPayment(HttpStatus status, CreditAccountPaymentResponse response) throws JsonProcessingException {

        var orderSummary = orderSummaryWithFee();
        CreditAccountPaymentRequest request = getCreditAccountPaymentRequest(orderSummary);

        PAYMENTS_SERVER.stubFor(post("/credit-account-payments")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            // .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withRequestBody(equalToJson(OBJECT_MAPPER.writeValueAsString(request)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OBJECT_MAPPER.writeValueAsString(response))));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("payment.service.api.baseurl=" + "http://localhost:" + PAYMENTS_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }

    private static CreditAccountPaymentRequest getCreditAccountPaymentRequest(OrderSummary orderSummary) {
        var request = new CreditAccountPaymentRequest();
        request.setService("divorce");
        request.setCurrency("GBP");
        request.setAmount(orderSummary.getPaymentTotal());
        request.setCcdCaseNumber(TEST_CASE_ID.toString());
        request.setAccountNumber("PBA0012345");
        request.setOrganisationName("Test Organisation");
        request.setDescription("fees for divorce");

        ListValue<Fee> feeItem = orderSummary.getFees().get(0);
        Fee fee = feeItem.getValue();

        PaymentItem paymentItem = getPaymentItem(orderSummary, fee);

        request.setFees(singletonList(paymentItem));

        return request;
    }

    private static PaymentItem getPaymentItem(OrderSummary orderSummary, Fee fee) {
        return PaymentItem
            .builder()
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
            .calculatedAmount(orderSummary.getPaymentTotal())
            .code(fee.getCode())
            .version(fee.getVersion())
            .build();
    }
}
