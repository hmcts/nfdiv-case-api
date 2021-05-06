package uk.gov.hmcts.divorce.citizen;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.citizen.event.PaymentMade.PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.PetitionerStatementOfTruth.PETITIONER_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

@SpringBootTest
public class PaymentMadeTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-payment-made.json";
    private static final String RESPONSE = "classpath:responses/ccd-callback-casedata-payment-made.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValid() throws IOException {
        REQUEST.replace("2020-04-28", LocalDate.now().minus(1, YEARS).minus(1, DAYS).toString());
        RESPONSE.replace("2020-04-28", LocalDate.now().minus(1, YEARS).minus(1, DAYS).toString());

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(PAYMENT_MADE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .id(1234567890123456L)
                            .data(caseData(REQUEST))
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse(RESPONSE),
            response.asString(),
            new CustomComparator(STRICT, new Customization("data.dateSubmitted", (o1, o2) -> true))
        );
    }

}
