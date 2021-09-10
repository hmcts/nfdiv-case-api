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
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.testutil.CaseDataWireMock;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataWireMock.stubForCcdCaseRoles;
import static uk.gov.hmcts.divorce.testutil.CaseDataWireMock.stubForCcdCaseRolesUpdateFailure;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesNotFound;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SOLICITOR_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamFailure;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreditAccountPayment;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubPbaNumbersRetrieval;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponseAsJson;
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
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryAndSolicitorRolesAreSet()
        throws Exception {

        stubForFeesLookup(getFeeResponseAsJson());
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamDetails(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(SYSTEM_UPDATE_AUTH_TOKEN);
        stubPbaNumbersRetrieval();

        when(serviceTokenGenerator.generate()).thenReturn(AUTH_HEADER_VALUE);

        stubForCcdCaseRoles();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdCallbackResponse())
            );

        verify(serviceTokenGenerator,times(2)).generate();
        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), SOLICITOR_SUBMIT)))
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
    public void givenValidCaseDataWhenCallbackIsInvokedAndIdamUserRetrievalThrowsUnauthorizedThen401IsReturned()
        throws Exception {

        stubForFeesLookup(getFeeResponseAsJson());
        stubForIdamFailure();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Unauthorized.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("Invalid idam credentials")
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedAndCcdCaseRolesUpdateThrowsForbiddenExceptionThen403IsReturned()
        throws Exception {

        stubForFeesLookup(getFeeResponseAsJson());
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamDetails(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(CASEWORKER_AUTH_TOKEN);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        stubForCcdCaseRolesUpdateFailure();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), SOLICITOR_SUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
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
                .caseReference(TEST_CASE_ID.toString())
                .build()
        );

        var data = caseDataWithStatementOfTruth();
        data.getApplication().setApplicationPayments(null);
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

    private String expectedCcdCallbackResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/issue-fees-response.json");
    }

    private String expectedCcdAboutToSubmitCallbackResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth.json");
    }

    private String expectedCcdAboutToSubmitCallbackErrorResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth-error.json");
    }
}
