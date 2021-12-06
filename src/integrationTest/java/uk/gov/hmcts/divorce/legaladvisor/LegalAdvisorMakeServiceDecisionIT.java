package uk.gov.hmcts.divorce.legaladvisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class
})
public class LegalAdvisorMakeServiceDecisionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Clock clock;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "NFD_Service_Order.docx");

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DISPENSED)
                    .serviceApplicationGranted(YES)
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-dispensed-response.json"
            )));
    }

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateDeemedServiceOrderDocIfApplicationIsGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "NFD_Service_Order.docx");

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(YES)
                    .deemedServiceDate(LocalDate.of(2021, 6, 20))
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-deemed-response.json"
            )));
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndSetDecisionDateIfApplicationIsNotGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(NO)
                    .deemedServiceDate(LocalDate.of(2021, 6, 20))
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-deemed-not-granted-response.json"
            )));
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndSetDecisionDateIfApplicationIsNotGrantedAndTypeIsDispensed()
        throws Exception {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DISPENSED)
                    .serviceApplicationGranted(NO)
                    .deemedServiceDate(LocalDate.of(2021, 6, 20))
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-dispensed-not-granted-response.json"
            )));
    }
}
