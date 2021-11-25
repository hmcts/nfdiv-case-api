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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAnswerReceived.CASEWORKER_ADD_ANSWER;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerAnswerReceivedIT {

    private static final String CASEWORKER_ANSWERS_RECEIVED_RESPONSE =
        "classpath:caseworker-answers-received-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldChangeCaseStateToHoldingAndSetDueDateIfSuccessfullyServed() throws Exception {

        final DivorceDocument d11 = DivorceDocument.builder()
            .documentDateAdded(LocalDate.now())
            .documentLink(
                Document
                    .builder()
                    .url("http://localhost:4200/assets/d11")
                    .filename("d11.pdf")
                    .binaryUrl("d11.pdf/binary")
                    .build()
            )
            .documentType(DocumentType.D11)
            .build();

        final CaseData caseData = CaseData.builder()
            .documentsUploaded(new ArrayList<>())
            .uploadD11Document(d11)
            .alternativeService(
                AlternativeService.builder()
                    .paymentMethod(FEE_PAY_BY_ACCOUNT)
                    .feeAccountNumber("FEE0233")
                    .feeAccountReferenceNumber("Ref1")
                    .build()
            )
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_ADD_ANSWER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_ADD_ANSWER)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(CASEWORKER_ANSWERS_RECEIVED_RESPONSE)));
    }
}
