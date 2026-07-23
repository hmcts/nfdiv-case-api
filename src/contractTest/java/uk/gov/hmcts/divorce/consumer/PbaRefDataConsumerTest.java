package uk.gov.hmcts.divorce.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaRefDataClient;

import java.util.HashMap;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@PactDirectory("pacts")
@PactTestFor(providerName = "referenceData_organisationalExternalPbas", port = "8892")
public class PbaRefDataConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String EMAIL_KEY = "UserEmail";

    @Autowired
    private PbaRefDataClient pbaRefDataClient;

    @Pact(provider = "referenceData_organisationalExternalPbas", consumer = "nfdiv_caseApi")
    public V4Pact createPbaRetrievalFragment(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("Pbas organisational data exists for identifier " + ORGANISATION_EMAIL)
            .uponReceiving("a request for information for that organisation's pbas")
            .path("/refdata/external/v1/organisations/pbas")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN, EMAIL_KEY, ORGANISATION_EMAIL)
            .willRespondWith()
            .status(200)
            .headers(getHeadersMap())
            .body(buildOrganisationalResponsePactDsl())
            .toPact(V4Pact.class);
    }

    private DslPart buildOrganisationalResponsePactDsl() {
        return newJsonBody(o -> {
            o.object("organisationEntityResponse", ob -> ob
                .stringType("organisationIdentifier", ORGANISATION_EMAIL)
                .stringMatcher("status",
                    "PENDING|ACTIVE|BLOCKED|DELETED", "ACTIVE")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", true)
                .stringType("companyNumber", "123456")
                .stringType("companyUrl", "somecompany@org.com")
                .array("paymentAccount", pa ->
                    pa.stringType("paymentAccountA1"))
                .object("superUser", su -> su
                    .stringType("firstName", "firstName")
                    .stringType("lastName", "lastName")
                    .stringType("email", "emailAddress"))
            );
        }).build();
    }

    private Map<String, String> getHeadersMap() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Test
    @PactTestFor(pactMethod = "createPbaRetrievalFragment")
    void verifyGetOrganisationalPbaReferenceDataPact() throws JSONException {
        ResponseEntity<PbaOrganisationResponse> response = pbaRefDataClient.retrievePbaNumbers(
            SOME_AUTHORIZATION_TOKEN, SOME_SERVICE_AUTHORIZATION_TOKEN, ORGANISATION_EMAIL);
        assertThat(response.getBody().getOrganisationEntityResponse().getOrganisationIdentifier())
            .isEqualTo(ORGANISATION_EMAIL);
    }
}
