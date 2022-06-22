package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.CASEWORKER_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.start;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseworkerConfirmServicePaymentIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    PaymentService paymentService;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        start();
    }

    @AfterAll
    static void tearDown() {
        stopAndReset();
    }

    @Test
    void shouldSetStateToAwaitingServiceConsiderationForDeemedAndDispensed() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);

        caseData.getAlternativeService().setAlternativeServiceType(AlternativeServiceType.DEEMED);
        caseData.getAlternativeService().getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_PHONE);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=AltPaymentSummary")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_SERVICE_PAYMENT)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.state").value("AwaitingServiceConsideration"));
    }

    @Test
    void shouldSetStateToAwaitingBailiffReferralForBailiffService() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);

        caseData.getAlternativeService().setAlternativeServiceType(AlternativeServiceType.BAILIFF);
        caseData.getAlternativeService().getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_PHONE);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=AltPaymentSummary")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_SERVICE_PAYMENT)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.state").value("AwaitingBailiffReferral"));
    }
}
