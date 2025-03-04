package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.payment.model.CasePaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreateServiceRequestBody;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;
import uk.gov.hmcts.divorce.payment.model.ServiceReferenceResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.OrganisationEntityResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaOrganisationResponse;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.penceToPounds;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.HMCTS_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

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

    public static void stubCreateServiceRequest(HttpStatus status, CreateServiceRequestBody request) throws JsonProcessingException {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        PAYMENTS_SERVER.stubFor(post("/service-request")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withRequestBody(new EqualToJsonPattern(OBJECT_MAPPER.writeValueAsString(request), true, true))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OBJECT_MAPPER.writeValueAsString(
                    ServiceReferenceResponse.builder().serviceRequestReference(TEST_SERVICE_REFERENCE).build())
                ))
        );
    }

    public static void stubCreditAccountPayment(
        HttpStatus status, CreditAccountPaymentResponse response, OrderSummary orderSummary
    ) throws JsonProcessingException {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CreditAccountPaymentRequest request = getCreditAccountPaymentRequest(orderSummary);

        PAYMENTS_SERVER.stubFor(post("/service-request/test-service-reference/pba-payments")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withRequestBody(new EqualToJsonPattern(OBJECT_MAPPER.writeValueAsString(request), true, true))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OBJECT_MAPPER.writeValueAsString(response))));
    }

    public static void stubPbaNumbersRetrieval() throws JsonProcessingException {

        var pbaOrganisationResponse = PbaOrganisationResponse.builder().build();
        pbaOrganisationResponse.setOrganisationEntityResponse(
            OrganisationEntityResponse
                .builder()
                .paymentAccount(List.of("PBA0012345"))
                .build()
        );

        PAYMENTS_SERVER.stubFor(get("/refdata/external/v1/organisations/pbas")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(AUTH_HEADER_VALUE))
            .withHeader("UserEmail", new EqualToPattern(TEST_USER_EMAIL))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OBJECT_MAPPER.writeValueAsString(pbaOrganisationResponse))));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("payment.service.api.baseurl=" + "http://localhost:" + PAYMENTS_SERVER.port())
                .and("pba.ref.data.service.url=" + "http://localhost:" + PAYMENTS_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }

    public static CreateServiceRequestBody buildServiceReferenceRequest(CaseData data, String responsibleParty) {
        return CreateServiceRequestBody.builder()
            .ccdCaseNumber(TEST_CASE_ID)
            .caseReference(TEST_CASE_ID)
            .callBackUrl(data.getCitizenPaymentCallbackUrl())
            .hmctsOrgId(HMCTS_ORG_ID)
            .fees(List.of(
                PaymentItem.builder()
                    .ccdCaseNumber(TEST_CASE_ID.toString())
                    .calculatedAmount("10")
                    .version("1")
                    .code(FEE_CODE)
                    .build()
            ))
            .casePaymentRequest(
                CasePaymentRequest.builder()
                    .responsibleParty(responsibleParty)
                    .action("payment")
                    .build()
            ).build();
    }

    private static CreditAccountPaymentRequest getCreditAccountPaymentRequest(OrderSummary orderSummary) {
        var request = CreditAccountPaymentRequest.builder()
            .currency("GBP")
            .amount(penceToPounds(orderSummary.getPaymentTotal()))
            .accountNumber("PBA0012345")
            .organisationName("Test Organisation")
            .customerReference(null)
            .build();

        return request;
    }

    private static PaymentItem getPaymentItem(Fee fee) {
        return PaymentItem
            .builder()
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
            .calculatedAmount(fee.getAmount())
            .code(fee.getCode())
            .version(fee.getVersion())
            .build();
    }
}
