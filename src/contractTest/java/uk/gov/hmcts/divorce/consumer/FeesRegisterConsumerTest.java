package uk.gov.hmcts.divorce.consumer;


import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import org.apache.hc.core5.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.divorce.payment.client.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.BAILIFF_SERVE_DOC_FEE_CODE;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.DIVORCE_APPLICATION_FEE_CODE;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.FINANCIAL_ORDER_ON_NOTICE_FEE_CODE;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_CODE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.DEFAULT_CHANNEL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_MISC;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.FAMILY;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.FAMILY_COURT;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_FINANCIAL_ORDER_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@PactDirectory("pacts")
@PactTestFor(providerName = "feeRegister_lookUp", port = "4411")
@TestPropertySource(locations = {"/application.properties"})
public class FeesRegisterConsumerTest {

    @Autowired
    FeesAndPaymentsClient feesClient;

    @Pact(provider = "feeRegister_lookUp", consumer = "nfdiv_caseApi")
    public V4Pact createApplicationFeeFragment(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("service is registered in Fee registry")
            .uponReceiving("request for Divorce application fee")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("channel", DEFAULT_CHANNEL, DEFAULT_CHANNEL)
            .matchQuery("event", EVENT_ISSUE, EVENT_ISSUE)
            .matchQuery("jurisdiction1", FAMILY, FAMILY)
            .matchQuery("jurisdiction2", FAMILY_COURT, FAMILY_COURT)
            .matchQuery("service", SERVICE_DIVORCE, SERVICE_DIVORCE)
            .matchQuery("keyword", KEYWORD_DIVORCE, KEYWORD_DIVORCE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(new PactDslJsonBody()
                .stringValue("code", DIVORCE_APPLICATION_FEE_CODE))
            .status(HttpStatus.SC_OK)
            .toPact(V4Pact.class);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "nfdiv_caseApi")
    public V4Pact createGeneralApplicationFeeWithoutNoticeFragment(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("service is registered in Fee registry")
            .uponReceiving("request for General application fee")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("channel", DEFAULT_CHANNEL, DEFAULT_CHANNEL)
            .matchQuery("event", "general application", "general application")
            .matchQuery("jurisdiction1", FAMILY, FAMILY)
            .matchQuery("jurisdiction2", FAMILY_COURT, FAMILY_COURT)
            .matchQuery("service", SERVICE_OTHER, SERVICE_OTHER)
            .matchQuery("keyword", KEYWORD_WITHOUT_NOTICE, KEYWORD_WITHOUT_NOTICE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(new PactDslJsonBody()
                .stringValue("code", GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_CODE))
            .status(HttpStatus.SC_OK)
            .toPact(V4Pact.class);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "nfdiv_caseApi")
    public V4Pact createFinancialOrderOnNoticeFeeFragment(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("service is registered in Fee registry")
            .uponReceiving("request for Financial Order on notice fee")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("channel", DEFAULT_CHANNEL, DEFAULT_CHANNEL)
            .matchQuery("event", EVENT_MISC, EVENT_MISC)
            .matchQuery("jurisdiction1", FAMILY, FAMILY)
            .matchQuery("jurisdiction2", FAMILY_COURT, FAMILY_COURT)
            .matchQuery("service", SERVICE_OTHER, SERVICE_OTHER)
            .matchQuery("keyword", KEYWORD_FINANCIAL_ORDER_NOTICE, KEYWORD_FINANCIAL_ORDER_NOTICE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(new PactDslJsonBody()
                .stringValue("code", FINANCIAL_ORDER_ON_NOTICE_FEE_CODE))
            .status(HttpStatus.SC_OK)
            .toPact(V4Pact.class);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "nfdiv_caseApi")
    public V4Pact createBailiffServiceFeeFragment(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("service is registered in Fee registry")
            .uponReceiving("request for Bailiff service fee")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("channel", DEFAULT_CHANNEL, DEFAULT_CHANNEL)
            .matchQuery("event", EVENT_ENFORCEMENT, EVENT_ENFORCEMENT)
            .matchQuery("jurisdiction1", FAMILY, FAMILY)
            .matchQuery("jurisdiction2", FAMILY_COURT, FAMILY_COURT)
            .matchQuery("service", SERVICE_OTHER, SERVICE_OTHER)
            .matchQuery("keyword", KEYWORD_BAILIFF, KEYWORD_BAILIFF)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(new PactDslJsonBody()
                .stringValue("code", BAILIFF_SERVE_DOC_FEE_CODE))
            .status(HttpStatus.SC_OK)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createApplicationFeeFragment")
    public void verifyApplicationFeeServicePact() throws JSONException {
        FeeResponse result = feesClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            EVENT_ISSUE,
            FAMILY,
            FAMILY_COURT,
            SERVICE_DIVORCE,
            KEYWORD_DIVORCE
        );

        assertThat(DIVORCE_APPLICATION_FEE_CODE).isEqualTo(result.getFeeCode());
    }

    @Test
    @PactTestFor(pactMethod = "createGeneralApplicationFeeWithoutNoticeFragment")
    public void verifyGeneralApplicationFeeWithoutNoticeServicePact() throws JSONException {
        FeeResponse result = feesClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            EVENT_GENERAL,
            FAMILY,
            FAMILY_COURT,
            SERVICE_OTHER,
            KEYWORD_WITHOUT_NOTICE
        );

        assertThat(GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_CODE).isEqualTo(result.getFeeCode());
    }

    @Test
    @PactTestFor(pactMethod = "createFinancialOrderOnNoticeFeeFragment")
    public void verifyFinancialOrderOnNoticeFeeServicePact() throws JSONException {
        FeeResponse result = feesClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            EVENT_MISC,
            FAMILY,
            FAMILY_COURT,
            SERVICE_OTHER,
            KEYWORD_FINANCIAL_ORDER_NOTICE
        );

        assertThat(FINANCIAL_ORDER_ON_NOTICE_FEE_CODE).isEqualTo(result.getFeeCode());
    }

    @Test
    @PactTestFor(pactMethod = "createBailiffServiceFeeFragment")
    public void verifyBailiffServiceFeeServicePact() throws JSONException {
        FeeResponse result = feesClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            EVENT_ENFORCEMENT,
            FAMILY,
            FAMILY_COURT,
            SERVICE_OTHER,
            KEYWORD_BAILIFF
        );

        assertThat(BAILIFF_SERVE_DOC_FEE_CODE).isEqualTo(result.getFeeCode());
    }
}
