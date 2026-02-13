package uk.gov.hmcts.divorce.consumer;


import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.hc.core5.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.divorce.payment.client.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;

import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.DIVORCE_APPLICATION_FEE_CODE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.DEFAULT_CHANNEL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.FAMILY;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.FAMILY_COURT;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_DIVORCE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class FeesRegisterConsumerTest {

    @Autowired
    FeesAndPaymentsClient feesClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("feeRegister_lookUp", "localhost", 8889, this);

    @Pact(provider = "feeRegister_lookUp", consumer = "nfdiv_case_api")
    public RequestResponsePact createApplicationFeeFragment(PactDslWithProvider builder) {
        return builder
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
            .toPact();
    }

    @Test
    @PactVerification(fragment = "createApplicationFeeFragment")
    public void verifyApplicationFeeServicePact() throws JSONException {
        FeeResponse result = feesClient.getPaymentServiceFee(
            DEFAULT_CHANNEL,
            EVENT_ISSUE,
            FAMILY,
            FAMILY_COURT,
            SERVICE_DIVORCE,
            KEYWORD_DIVORCE
        );

        assert(DIVORCE_APPLICATION_FEE_CODE).equals(result.getFeeCode());
    }
}
