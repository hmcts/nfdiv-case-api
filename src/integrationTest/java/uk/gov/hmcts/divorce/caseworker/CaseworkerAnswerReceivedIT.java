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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.citizen.notification.DisputedApplicationAnswerReceivedNotification;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAnswerReceived.CASEWORKER_ADD_ANSWER;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
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
    private NotificationDispatcher notificationDispatcher;

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
    public void shouldAddD11DocumentToDocumentsUploaded() throws Exception {

        final DivorceDocument d11 = DivorceDocument.builder()
            .documentDateAdded(LocalDate.now())
            .documentLink(
                Document.builder()
                    .url("http://localhost:4200/assets/d11")
                    .filename("d11.pdf")
                    .binaryUrl("d11.pdf/binary").build()
            )
            .documentType(DocumentType.D11)
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(new ArrayList<>())
                .answerReceivedSupportingDocuments(
                    List.of(ListValue.<DivorceDocument>builder().value(d11).build())
                ).build()
            ).alternativeService(AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .paymentMethod(FEE_PAY_BY_ACCOUNT)
                        .accountNumber("FEE0233")
                        .accountReferenceNumber("Ref1")
                        .build()
                ).build()
            ).build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_ADD_ANSWER, Holding.toString())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse).isEqualTo(json(expectedResponse(CASEWORKER_ANSWERS_RECEIVED_RESPONSE)));
    }

    @Test
    public void shouldSendNotification() throws Exception {

        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(
                AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE).build()
            ).build();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_ADD_ANSWER)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationDispatcher).send(any(DisputedApplicationAnswerReceivedNotification.class), any(CaseData.class), anyLong());
    }
}
