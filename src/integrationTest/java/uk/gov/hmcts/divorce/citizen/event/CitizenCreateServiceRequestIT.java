package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.buildServiceReferenceRequest;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreateServiceRequest;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    PaymentWireMock.PropertiesInitializer.class
})
public class CitizenCreateServiceRequestIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeAll
    static void setUp() {
        PaymentWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        PaymentWireMock.stopAndReset();
    }

    @Test
    public void givenCaseInAwaitingPaymentThenSetApplicationFeeServiceReference() throws Exception {
        var data = validApplicant1CaseData();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary());

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubCreateServiceRequest(OK, buildServiceReferenceRequest(data, data.getApplicant1()));

        triggerCitizenCreateServiceRequest(data, AwaitingPayment)
            .andExpect(jsonPath("$.data.applicationFeeServiceRequestReference")
                .value(TEST_SERVICE_REFERENCE));
    }

    @Test
    public void givenCaseInAwaitingFinalOrderPaymentThenSetFinalOrderFeeServiceReference() throws Exception {
        var data = validApplicant1CaseData();
        data.getFinalOrder().setApplicant2FinalOrderFeeOrderSummary(orderSummary());

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubCreateServiceRequest(OK, buildServiceReferenceRequest(data, data.getApplicant1()));

        triggerCitizenCreateServiceRequest(data, AwaitingFinalOrderPayment)
            .andExpect(jsonPath("$.data.applicant2FinalOrderFeeServiceRequestReference")
                .value(TEST_SERVICE_REFERENCE));
    }

    private ResultActions triggerCitizenCreateServiceRequest(CaseData caseData, State caseState) throws Exception {
        return mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CITIZEN_CREATE_SERVICE_REQUEST,
                        caseState.toString()
                    ))
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private static ListValue<Fee> getFeeListValue() {
        return ListValue
            .<Fee>builder()
            .value(Fee
                .builder()
                .amount("1000")
                .code(FEE_CODE)
                .version("1")
                .build()
            )
            .build();
    }

    private static OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("1000")
            .fees(singletonList(getFeeListValue()))
            .build();
    }
}
