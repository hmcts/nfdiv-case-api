package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;
import uk.gov.hmcts.divorce.testutil.CaseDataWireMock;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesNotFound;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.buildServiceReferenceRequest;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreateServiceRequest;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreditAccountPayment;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOL_PAYMENT_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.orderSummaryWithFee;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    FeesWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    CaseDataWireMock.PropertiesInitializer.class,
    PaymentWireMock.PropertiesInitializer.class})
public class SolicitorSubmitApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private PbaService pbaService;

    @MockBean
    private NotificationService notificationService;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
        CaseDataWireMock.start();
        FeesWireMock.start();
        PaymentWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
        CaseDataWireMock.stopAndReset();
        FeesWireMock.stopAndReset();
        PaymentWireMock.stopAndReset();
    }

    @Test
    public void createsOrderSummaryToPrepareCaseForPayment() throws Exception {
        var data = caseDataWithStatementOfTruth();
        data.getApplication().setApplicationFeeOrderSummary(null);
        data.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("test").build());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());
        stubCreateServiceRequest(OK, buildServiceReferenceRequest(data, data.getApplicant1().getFullName()));

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationFeeOrderSummary.PaymentTotal")
                .value("1000")
            )
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Test
    public void createsServiceRequestToPrepareCaseForPayment() throws Exception {
        var data = caseDataWithOrderSummary();
        data.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        var serviceRequestBody = buildServiceReferenceRequest(data, data.getApplicant1().getFullName());
        serviceRequestBody.setFees(List.of(
            PaymentItem.builder()
                .ccdCaseNumber(TEST_CASE_ID.toString())
                .calculatedAmount("550")
                .code("FEE002")
                .build()
        ));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(pbaService.populatePbaDynamicList()).thenReturn(new DynamicList());
        stubCreateServiceRequest(OK, serviceRequestBody);

        mockMvc.perform(post(SOL_PAYMENT_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationFeeServiceRequestReference")
                .value(TEST_SERVICE_REFERENCE)
            )
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        CaseData data = new CaseData();
        data.getApplication().setApplicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE);
        data.setApplicant1(
            Applicant.builder()
                .address(AddressGlobalUK.builder().addressLine1("test").build())
                .build()
        );

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isNotFound()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.NotFound.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("404 Fee event not found")
            );
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedThenStateIsChangedAndEmailIsSentToApplicant()
        throws Exception {

        stubCreditAccountPayment(
            CREATED,
            CreditAccountPaymentResponse
                .builder()
                .status(SUCCESS.toString())
                .build(),
            orderSummaryWithFee()
        );

        var data = caseDataWithStatementOfTruth();
        data.getApplication().setApplicationPayments(null);
        data.getApplication().setApplicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE);
        data.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        data.getApplication().setPbaNumbers(getPbaNumbersForAccount("PBA0012345"));
        data.getApplication().setApplicationFeeOrderSummary(orderSummaryWithFee());
        data.getApplicant1().getSolicitor().setOrganisationPolicy(organisationPolicy());

        MvcResult mvcResult = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(
                    data,
                    SOLICITOR_SUBMIT,
                    Draft.name())))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        assertThatJson(mvcResult.getResponse().getContentAsString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackResponse()));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenInValidCaseDataWhenAboutToSubmitCallbackIsInvokedThenStateIsNotChangedAndErrorIsReturned()
        throws Exception {

        var caseData = caseDataWithOrderSummary();
        caseData.getApplication().setApplicant1StatementOfTruth(null);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(
                    caseData,
                    SOLICITOR_SUBMIT,
                    Draft.name())))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackErrorResponse())
            );

        verifyNoInteractions(notificationService);
    }

    private String expectedCcdAboutToSubmitCallbackResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth.json");
    }

    private String expectedCcdAboutToSubmitCallbackErrorResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth-error.json");
    }
}
